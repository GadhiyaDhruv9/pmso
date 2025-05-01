package com.pmso.projectManagementSystemOne.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pmso.projectManagementSystemOne.config.JWTGenerator;
import com.pmso.projectManagementSystemOne.dto.LoginDto;
import com.pmso.projectManagementSystemOne.dto.RegisterDto;
import com.pmso.projectManagementSystemOne.dto.UserResponseDto;
import com.pmso.projectManagementSystemOne.entity.DocumentMaster;
import com.pmso.projectManagementSystemOne.entity.Role;
import com.pmso.projectManagementSystemOne.entity.UserDocument;
import com.pmso.projectManagementSystemOne.entity.UserEntity;
import com.pmso.projectManagementSystemOne.repository.DocumentMasterRepository;
import com.pmso.projectManagementSystemOne.repository.RoleRepository;
import com.pmso.projectManagementSystemOne.repository.UserDocumentRepository;
import com.pmso.projectManagementSystemOne.repository.UserRepository;
import com.pmso.projectManagementSystemOne.utils.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder encoder;
    private final JWTGenerator jwtGen;
    private final UserDocumentRepository userDocumentRepository;
    private final DocumentMasterRepository documentMasterRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.base-url}")
    private String fileBaseUrl;

    @Autowired
    public AuthController(AuthenticationManager authManager, UserRepository userRepo,
                          RoleRepository roleRepo, PasswordEncoder encoder, JWTGenerator jwtGen,
                          UserDocumentRepository userDocumentRepository, DocumentMasterRepository documentMasterRepository) {
        this.authManager = authManager;
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.encoder = encoder;
        this.jwtGen = jwtGen;
        this.userDocumentRepository = userDocumentRepository;
        this.documentMasterRepository = documentMasterRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDto dto) {
        try {
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getUsernameOrEmail(), dto.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(auth);
            String token = jwtGen.generateToken(auth);
            List<String> roles = auth.getAuthorities().stream()
                    .map(a -> a.getAuthority().replace("ROLE_", ""))
                    .collect(Collectors.toList());

            Map<String, Object> data = Map.of("token", token, "roles", roles);
            return ResponseUtil.success("Login successful", data);
        } catch (BadCredentialsException e) {
            return ResponseUtil.fail("Invalid username or password", e.getMessage(), HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return ResponseUtil.fail("Login failed", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value = "/register/admin", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registerAdmin(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("email") String email,
            @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture,
            @RequestPart(value = "panCard", required = false) MultipartFile panCard,
            @RequestPart(value = "aadharCard", required = false) MultipartFile aadharCard,
            @RequestPart(value = "proofOfAddress", required = false) MultipartFile proofOfAddress,
            @RequestPart(value = "bankDetails", required = false) MultipartFile bankDetails) {

        RegisterDto dto = new RegisterDto();
        dto.setUsername(username);
        dto.setPassword(password);
        dto.setEmail(email);

        if (profilePicture != null && !profilePicture.isEmpty()) dto.setProfilePicture(profilePicture);
        if (panCard != null && !panCard.isEmpty()) dto.setPanCard(panCard);
        if (aadharCard != null && !aadharCard.isEmpty()) dto.setAadharCard(aadharCard);
        if (proofOfAddress != null && !proofOfAddress.isEmpty()) dto.setProofOfAddress(proofOfAddress);
        if (bankDetails != null && !bankDetails.isEmpty()) dto.setBankDetails(bankDetails);

        return registerUser(dto, "ADMIN");
    }

    @PostMapping(value = "/register/manager", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registerManager(
            @RequestPart("userData") String userDataStr,
            @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture,
            @RequestPart(value = "panCard", required = false) MultipartFile panCard,
            @RequestPart(value = "aadharCard", required = false) MultipartFile aadharCard,
            @RequestPart(value = "proofOfAddress", required = false) MultipartFile proofOfAddress,
            @RequestPart(value = "bankDetails", required = false) MultipartFile bankDetails) {

        try {
            RegisterDto dto = new ObjectMapper().readValue(userDataStr, RegisterDto.class);
            dto.setProfilePicture(profilePicture);
            dto.setPanCard(panCard);
            dto.setAadharCard(aadharCard);
            dto.setProofOfAddress(proofOfAddress);
            dto.setBankDetails(bankDetails);
            return registerUser(dto, "MANAGER");
        } catch (Exception e) {
            return ResponseUtil.fail("Invalid request data", e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "/register/user", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registerUser(
            @RequestPart("userData") String userDataStr,
            @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture,
            @RequestPart(value = "panCard", required = false) MultipartFile panCard,
            @RequestPart(value = "aadharCard", required = false) MultipartFile aadharCard,
            @RequestPart(value = "proofOfAddress", required = false) MultipartFile proofOfAddress,
            @RequestPart(value = "bankDetails", required = false) MultipartFile bankDetails) {

        try {
            RegisterDto dto = new ObjectMapper().readValue(userDataStr, RegisterDto.class);
            dto.setProfilePicture(profilePicture);
            dto.setPanCard(panCard);
            dto.setAadharCard(aadharCard);
            dto.setProofOfAddress(proofOfAddress);
            dto.setBankDetails(bankDetails);
            return registerUser(dto, "USER");
        } catch (Exception e) {
            return ResponseUtil.fail("Invalid request data", e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    private ResponseEntity<?> registerUser(RegisterDto dto, String roleName) {
        if (userRepo.existsByUsername(dto.getUsername())) {
            return ResponseUtil.fail("Username already exists", "Conflict", HttpStatus.BAD_REQUEST);
        }

        if (dto.getEmail() != null && userRepo.existsByEmail(dto.getEmail())) {
            return ResponseUtil.fail("Email already exists", "Conflict", HttpStatus.BAD_REQUEST);
        }

        List<DocumentMaster> mandatoryDocs = documentMasterRepository.findAll().stream()
                .filter(DocumentMaster::getIsMandatory)
                .collect(Collectors.toList());

        Map<String, MultipartFile> uploadedFiles = Map.of(
                "profile", dto.getProfilePicture(),
                "pan", dto.getPanCard(),
                "aadhar", dto.getAadharCard(),
                "address", dto.getProofOfAddress(),
                "bank", dto.getBankDetails()
        );

        for (DocumentMaster doc : mandatoryDocs) {
            MultipartFile file = uploadedFiles.get(doc.getDocumentCode());
            if (file == null || file.isEmpty()) {
                return ResponseUtil.fail("Mandatory document missing: " + doc.getDocumentName(), "Bad Request", HttpStatus.BAD_REQUEST);
            }
        }

        UserEntity user = new UserEntity();
        user.setUsername(dto.getUsername());
        user.setPassword(encoder.encode(dto.getPassword()));
        user.setEmail(dto.getEmail());

        Role role = roleRepo.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        user.addRole(role);
        user.setCreatedBy(null);
        user.setUpdatedBy(null);

        UserEntity savedUser = userRepo.save(user);

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String profileUrl = processFileUpload(savedUser, dto.getProfilePicture(), "profile");
            String panUrl = processFileUpload(savedUser, dto.getPanCard(), "pan");
            String aadharUrl = processFileUpload(savedUser, dto.getAadharCard(), "aadhar");
            String addressUrl = processFileUpload(savedUser, dto.getProofOfAddress(), "address");
            String bankUrl = processFileUpload(savedUser, dto.getBankDetails(), "bank");

            UserResponseDto responseDto = new UserResponseDto(
                    savedUser.getUserId(),
                    savedUser.getUsername(),
                    savedUser.getEmail(),
                    savedUser.getRoles().stream().map(Role::getName).collect(Collectors.joining(",")),
                    null,
                    savedUser.getCreatedAt(),
                    savedUser.getUpdatedAt(),
                    null,
                    profileUrl,
                    panUrl,
                    aadharUrl,
                    addressUrl,
                    bankUrl
            );

            return ResponseUtil.success("User registered with role: " + roleName, responseDto);

        } catch (IOException e) {
            return ResponseUtil.fail("Failed to upload files", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String processFileUpload(UserEntity user, MultipartFile file, String docCode) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        DocumentMaster docMaster = documentMasterRepository.findByDocumentCode(docCode)
                .orElseThrow(() -> new RuntimeException("Document type not found: " + docCode));

        double fileSizeMb = file.getSize() / (1024.0 * 1024.0);
        if (fileSizeMb > docMaster.getMaxSizeMb()) {
            throw new IOException("File size exceeds limit for " + docMaster.getDocumentName() + ": " + docMaster.getMaxSizeMb() + "MB");
        }

        if (!docMaster.getIsAllowedMultiple()) {
            List<UserDocument> existingDocs = userDocumentRepository.findByUserId(user.getUserId());
            if (existingDocs.stream().anyMatch(doc -> doc.getDocumentMaster().getDocumentCode().equals(docCode))) {
                throw new IOException("Multiple uploads not allowed for " + docMaster.getDocumentName());
            }
        }

        String userFolderPath = uploadDir + user.getUsername() + "/";
        Path userDir = Paths.get(userFolderPath);
        if (!Files.exists(userDir)) {
            Files.createDirectories(userDir);
        }

        String originalFilename = Objects.requireNonNull(file.getOriginalFilename());
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String filename = docCode + "_" + System.currentTimeMillis() + fileExtension;

        Path filePath = Paths.get(userFolderPath + filename);
        Files.write(filePath, file.getBytes());

        UserDocument doc = new UserDocument();
        doc.setUserId(user.getUserId());
        doc.setUsername(user.getUsername());
        doc.setDocumentMaster(docMaster);
        doc.setFilePath(fileBaseUrl + user.getUsername() + "/" + filename);

        userDocumentRepository.save(doc);

        return fileBaseUrl + user.getUsername() + "/" + filename;
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        return userRepo.findById(id)
                .map(user -> {
                    List<UserDocument> documents = userDocumentRepository.findByUserId(user.getUserId());
                    Map<String, String> documentPaths = documents.stream()
                            .collect(Collectors.toMap(
                                    doc -> doc.getDocumentMaster().getDocumentCode(),
                                    UserDocument::getFilePath
                            ));

                    UserResponseDto dto = new UserResponseDto(
                            user.getUserId(),
                            user.getUsername(),
                            user.getEmail(),
                            user.getRoles().stream().map(Role::getName).collect(Collectors.joining(",")),
                            null,
                            user.getCreatedAt(),
                            user.getUpdatedAt(),
                            null,
                            documentPaths.get("profile"),
                            documentPaths.get("pan"),
                            documentPaths.get("aadhar"),
                            documentPaths.get("address"),
                            documentPaths.get("bank")
                    );
                    return ResponseUtil.success("User details fetched", dto);
                })
                .orElse(ResponseUtil.fail("User not found", "Invalid user ID", HttpStatus.NOT_FOUND));
    }
}