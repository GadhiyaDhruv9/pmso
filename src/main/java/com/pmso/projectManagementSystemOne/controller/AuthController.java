package com.pmso.projectManagementSystemOne.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pmso.projectManagementSystemOne.config.JWTGenerator;
import com.pmso.projectManagementSystemOne.dto.*;
import com.pmso.projectManagementSystemOne.entity.*;
import com.pmso.projectManagementSystemOne.repository.*;
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
import java.util.*;
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

    //USER LOGIN
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
            return ResponseUtil.success("Login successful", Map.of("token", token, "roles", roles));
        } catch (Exception e) {
            return ResponseUtil.fail("Login failed", e.getMessage(),
                    e instanceof BadCredentialsException ? HttpStatus.UNAUTHORIZED : HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //REGISTER ADMIN
    @PostMapping(value = "/register/admin", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registerAdmin(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("email") String email,
            @RequestPart(value = "profilePictures") List<MultipartFile> profilePictures,
            @RequestPart(value = "panCards") List<MultipartFile> panCards,
            @RequestPart(value = "aadharCards") List<MultipartFile> aadharCards,
            @RequestPart(value = "proofOfAddresses") List<MultipartFile> proofOfAddresses,
            @RequestPart(value = "bankDetails") List<MultipartFile> bankDetails) {

        RegisterDto dto = new RegisterDto();
        dto.setUsername(username);
        dto.setPassword(password);
        dto.setEmail(email);
        dto.setProfilePictures(nonNullList(profilePictures));
        dto.setPanCards(nonNullList(panCards));
        dto.setAadharCards(nonNullList(aadharCards));
        dto.setProofOfAddresses(nonNullList(proofOfAddresses));
        dto.setBankDetails(nonNullList(bankDetails));

        return registerUser(dto, "ADMIN");
    }

    //REGISTER MANAGER (NOT NEEDED)
    @PostMapping(value = "/register/manager", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registerManager(
            @RequestPart("userData") String userDataStr,
            @RequestPart(value = "profilePictures") List<MultipartFile> profilePictures,
            @RequestPart(value = "panCards") List<MultipartFile> panCards,
            @RequestPart(value = "aadharCards") List<MultipartFile> aadharCards,
            @RequestPart(value = "proofOfAddresses") List<MultipartFile> proofOfAddresses,
            @RequestPart(value = "bankDetails") List<MultipartFile> bankDetails) {

        try {
            RegisterDto dto = new ObjectMapper().readValue(userDataStr, RegisterDto.class);
            dto.setProfilePictures(nonNullList(profilePictures));
            dto.setPanCards(nonNullList(panCards));
            dto.setAadharCards(nonNullList(aadharCards));
            dto.setProofOfAddresses(nonNullList(proofOfAddresses));
            dto.setBankDetails(nonNullList(bankDetails));
            return registerUser(dto, "MANAGER");
        } catch (Exception e) {
            return ResponseUtil.fail("Invalid request data", e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    //REGISTER USER
    @PostMapping(value = "/register/user", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registerUser(
            @RequestPart("userData") String userDataStr,
            @RequestPart(value = "profilePictures") List<MultipartFile> profilePictures,
            @RequestPart(value = "panCards") List<MultipartFile> panCards,
            @RequestPart(value = "aadharCards") List<MultipartFile> aadharCards,
            @RequestPart(value = "proofOfAddresses") List<MultipartFile> proofOfAddresses,
            @RequestPart(value = "bankDetails") List<MultipartFile> bankDetails) {

        try {
            RegisterDto dto = new ObjectMapper().readValue(userDataStr, RegisterDto.class);
            dto.setProfilePictures(nonNullList(profilePictures));
            dto.setPanCards(nonNullList(panCards));
            dto.setAadharCards(nonNullList(aadharCards));
            dto.setProofOfAddresses(nonNullList(proofOfAddresses));
            dto.setBankDetails(nonNullList(bankDetails));
            return registerUser(dto, "USER");
        } catch (Exception e) {
            return ResponseUtil.fail("Invalid request data", e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    private ResponseEntity<?> registerUser(RegisterDto dto, String roleName) {
        if (userRepo.existsByUsername(dto.getUsername())) {
            return ResponseUtil.fail("Username already exists", null, HttpStatus.BAD_REQUEST);
        }
        if (dto.getEmail() != null && userRepo.existsByEmail(dto.getEmail())) {
            return ResponseUtil.fail("Email already exists", null, HttpStatus.BAD_REQUEST);
        }

        Map<String, DocumentMaster> docTypes = documentMasterRepository.findAll().stream()
                .collect(Collectors.toMap(DocumentMaster::getDocumentCode, dm -> dm));

        Map<String, List<MultipartFile>> documents = Map.of(
                "profile", dto.getProfilePictures(),
                "pan", dto.getPanCards(),
                "aadhar", dto.getAadharCards(),
                "address", dto.getProofOfAddresses(),
                "bank", dto.getBankDetails()
        );

        for (Map.Entry<String, DocumentMaster> entry : docTypes.entrySet()) {
            DocumentMaster doc = entry.getValue();
            List<MultipartFile> files = documents.get(entry.getKey());

            if (doc.isMandatory() && (files == null || files.isEmpty() || files.stream().allMatch(f -> f.isEmpty()))) {
                return ResponseUtil.fail("Mandatory document missing: " + doc.getDocumentName(),
                        null, HttpStatus.BAD_REQUEST);
            }
        }

        UserEntity user = new UserEntity();
        user.setUsername(dto.getUsername());
        user.setPassword(encoder.encode(dto.getPassword()));
        user.setEmail(dto.getEmail());

        Role role = roleRepo.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        user.addRole(role);

        UserEntity savedUser = userRepo.save(user);

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Map<String, List<String>> documentUrls = new HashMap<>();
            for (Map.Entry<String, List<MultipartFile>> entry : documents.entrySet()) {
                List<String> urls = new ArrayList<>();
                DocumentMaster docType = docTypes.get(entry.getKey());

                for (MultipartFile file : entry.getValue()) {
                    if (!file.isEmpty()) {
                        String url = processFileUpload(savedUser, file, docType);
                        urls.add(url);
                    }
                }
                documentUrls.put(entry.getKey(), urls);
            }

            UserResponseDto responseDto = new UserResponseDto(
                    savedUser.getUserId(),
                    savedUser.getUsername(),
                    savedUser.getEmail(),
                    savedUser.getRoles().stream().map(Role::getName).collect(Collectors.joining(",")),
                    null,
                    savedUser.getCreatedAt(),
                    savedUser.getUpdatedAt(),
                    null,
                    documentUrls.get("profile"),
                    documentUrls.get("pan"),
                    documentUrls.get("aadhar"),
                    documentUrls.get("address"),
                    documentUrls.get("bank")
            );

            return ResponseUtil.success("User registered successfully", responseDto);
        } catch (IOException e) {
            return ResponseUtil.fail("Failed to process documents", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String processFileUpload(UserEntity user, MultipartFile file, DocumentMaster docType) throws IOException {
        if (file == null || file.isEmpty()) return null;

        String filename = file.getOriginalFilename();
        String ext = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        if (!Arrays.asList(docType.getAllowedExtensions().split(",")).contains(ext)) {
            throw new IOException("Invalid file extension for " + docType.getDocumentName());
        }

        double sizeMB = file.getSize() / (1024.0 * 1024.0);
        if (sizeMB > docType.getMaxSize()) {
            throw new IOException("File exceeds maximum size for " + docType.getDocumentName());
        }

        if (!docType.isAllowedMultiple() &&
                userDocumentRepository.existsByUserIdAndDocumentMaster_DocumentCode(user.getUserId(), docType.getDocumentCode())) {
            throw new IOException("Multiple uploads not allowed for " + docType.getDocumentName());
        }

        String userDir = uploadDir + user.getUsername() + "/";
        Files.createDirectories(Paths.get(userDir));

        String newFilename = docType.getDocumentCode() + "_" + System.currentTimeMillis() + "." + ext;
        Path filePath = Paths.get(userDir + newFilename);
        Files.write(filePath, file.getBytes());

        UserDocument doc = new UserDocument();
        doc.setUserId(user.getUserId());
        doc.setUsername(user.getUsername());
        doc.setDocumentMaster(docType);
        doc.setFilePath(fileBaseUrl + user.getUsername() + "/" + newFilename);
        userDocumentRepository.save(doc);

        return doc.getFilePath();
    }

    private <T> List<T> nonNullList(List<T> list) {
        return list != null ? list : Collections.emptyList();
    }
}