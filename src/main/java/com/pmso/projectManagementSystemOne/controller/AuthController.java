package com.pmso.projectManagementSystemOne.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pmso.projectManagementSystemOne.config.JWTGenerator;
import com.pmso.projectManagementSystemOne.dto.*;
import com.pmso.projectManagementSystemOne.entity.*;
import com.pmso.projectManagementSystemOne.repository.*;
import com.pmso.projectManagementSystemOne.utils.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.transaction.annotation.Transactional;
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

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
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
            return ResponseUtil.success("Login successful", Map.of("token", token, "roles", roles));
        } catch (Exception e) {
            logger.error("Login failed for user: {}, error: {}", dto.getUsernameOrEmail(), e.getMessage());
            return ResponseUtil.fail("Login failed", e.getMessage(),
                    e instanceof BadCredentialsException ? HttpStatus.UNAUTHORIZED : HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value = "/register/admin", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registerAdmin(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("email") String email,
            @RequestParam("documentTypes") List<String> documentTypes,
            @RequestParam("documents") List<MultipartFile> documents) throws IOException {

        RegisterDto dto = new RegisterDto();
        dto.setUsername(username);
        dto.setPassword(password);
        dto.setEmail(email);

        Map<String, List<MultipartFile>> docMap = new LinkedHashMap<>();
        for (int i = 0; i < documentTypes.size(); i++) {
            String docType = documentTypes.get(i);
            docMap.computeIfAbsent(docType, k -> new ArrayList<>()).add(documents.get(i));
        }

        dto.setDocuments(docMap);
        return registerUser(dto, "ADMIN");
    }

    @Transactional
    protected ResponseEntity<?> registerUser(RegisterDto dto, String roleName) {
        try {
            if (userRepo.existsByUsername(dto.getUsername())) {
                return ResponseUtil.fail("Username already exists", null, HttpStatus.BAD_REQUEST);
            }
            if (dto.getEmail() != null && userRepo.existsByEmail(dto.getEmail())) {
                return ResponseUtil.fail("Email already exists", null, HttpStatus.BAD_REQUEST);
            }

            Map<String, DocumentMaster> existingDocTypes = documentMasterRepository.findAll()
                    .stream()
                    .collect(Collectors.toMap(DocumentMaster::getDocumentCode, dm -> dm));

            List<String> missingDocs = existingDocTypes.values().stream()
                    .filter(DocumentMaster::isMandatory)
                    .filter(doc -> dto.getDocuments() == null ||
                            !dto.getDocuments().containsKey(doc.getDocumentCode()) ||
                            dto.getDocuments().get(doc.getDocumentCode()).isEmpty())
                    .map(DocumentMaster::getDocumentName)
                    .collect(Collectors.toList());

            if (!missingDocs.isEmpty()) {
                return ResponseUtil.fail("Mandatory documents missing: " + String.join(", ", missingDocs),
                        null, HttpStatus.BAD_REQUEST);
            }

            UserEntity user = new UserEntity();
            user.setUsername(dto.getUsername());
            user.setPassword(encoder.encode(dto.getPassword()));
            user.setEmail(dto.getEmail());

            Role role = roleRepo.findByName(roleName)
                    .orElseThrow(() -> new RuntimeException("Role not found"));
            user.addRole(role);

            Path uploadPath = Paths.get(uploadDir);
            Files.createDirectories(uploadPath);

            Map<String, List<String>> docTypeToFileUrls = new LinkedHashMap<>();
            List<UserDocument> savedDocuments = new ArrayList<>();
            UserEntity savedUser = userRepo.save(user);

            if (dto.getDocuments() != null) {
                for (Map.Entry<String, List<MultipartFile>> entry : dto.getDocuments().entrySet()) {
                    String docType = entry.getKey();
                    DocumentMaster docMaster = existingDocTypes.get(docType);
                    List<String> uploadedUrls = new ArrayList<>();

                    for (MultipartFile file : entry.getValue()) {
                        try {
                            String url = processFileUpload(savedUser, file, docMaster);
                            uploadedUrls.add(url);

                            UserDocument doc = new UserDocument();
                            doc.setUserId(savedUser.getUserId());
                            doc.setUsername(savedUser.getUsername());
                            doc.setDocumentMaster(docMaster);
                            doc.setFilePath(url);
                            UserDocument savedDoc = userDocumentRepository.save(doc);
                            savedDocuments.add(savedDoc);
                        } catch (IOException e) {
                            logger.error("Failed to process file for document type {}: {}", docType, e.getMessage());
                        }
                    }
                    docTypeToFileUrls.put(docType, uploadedUrls);
                }
            }

            List<UserDocumentDto> documentDtos = docTypeToFileUrls.entrySet().stream()
                    .map(entry -> {
                        String docType = entry.getKey();
                        List<String> urls = entry.getValue();

                        List<Long> docIds = savedDocuments.stream()
                                .filter(d -> d.getDocumentMaster().getDocumentCode().equals(docType))
                                .map(UserDocument::getId)
                                .collect(Collectors.toList());

                        return new UserDocumentDto(
                                docIds.isEmpty() ? null : docIds.get(0),
                                docType,
                                urls
                        );
                    })
                    .collect(Collectors.toList());

            UserResponseDto responseDto = new UserResponseDto(
                    savedUser.getUserId(),
                    savedUser.getUsername(),
                    savedUser.getEmail(),
                    savedUser.getRoles().stream().map(Role::getName).collect(Collectors.joining(",")),
                    null,
                    savedUser.getCreatedAt(),
                    savedUser.getUpdatedAt(),
                    null,
                    documentDtos
            );

            return ResponseUtil.success("User registered successfully", responseDto);
        } catch (Exception e) {
            logger.error("Registration failed: {}", e.getMessage());
            return ResponseUtil.fail("Registration failed", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String processFileUpload(UserEntity user, MultipartFile file, DocumentMaster docMaster) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("Empty file uploaded");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank()) {
            throw new IOException("Invalid file: No filename provided");
        }

        String ext = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        if (!Arrays.asList(docMaster.getAllowedExtensions().split(",")).contains(ext)) {
            throw new IOException("Invalid file extension: " + ext +
                    ". Allowed: " + docMaster.getAllowedExtensions());
        }

        double sizeMB = file.getSize() / (1024.0 * 1024.0);
        if (docMaster.getMaxSize() != null && sizeMB > docMaster.getMaxSize()) {
            throw new IOException("File exceeds maximum size: " + sizeMB + "MB. Max allowed: " +
                    docMaster.getMaxSize() + "MB");
        }

        String userDir = uploadDir + user.getUsername() + "/";
        Files.createDirectories(Paths.get(userDir));

        String newFilename = docMaster.getDocumentCode() + "_" +
                System.currentTimeMillis() + "_" +
                UUID.randomUUID() + "." + ext;
        Path filePath = Paths.get(userDir + newFilename);
        Files.write(filePath, file.getBytes());

        return fileBaseUrl + user.getUsername() + "/" + newFilename;
    }
}