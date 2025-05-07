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
            @RequestParam(value = "documentTypes", required = false) List<String> documentTypes,
            @RequestParam(value = "documents", required = false) List<MultipartFile> documents) {

        RegisterDto dto = new RegisterDto();
        dto.setUsername(username);
        dto.setPassword(password);
        dto.setEmail(email);
        if (documents != null && documentTypes != null) {
            Map<String, List<MultipartFile>> docMap = new HashMap<>();
            for (int i = 0; i < Math.min(documents.size(), documentTypes.size()); i++) {
                String docType = documentTypes.get(i).toLowerCase();
                docMap.computeIfAbsent(docType, k -> new ArrayList<>()).add(documents.get(i));
            }
            dto.setDocuments(docMap);
        }
        return registerUser(dto, "ADMIN");
    }

    @PostMapping(value = "/register/manager", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registerManager(
            @RequestPart("userData") String userDataStr,
            @RequestParam(value = "documentTypes", required = false) List<String> documentTypes,
            @RequestParam(value = "documents", required = false) List<MultipartFile> documents) throws Exception {

        RegisterDto dto = new ObjectMapper().readValue(userDataStr, RegisterDto.class);
        if (documents != null && documentTypes != null) {
            Map<String, List<MultipartFile>> docMap = new HashMap<>();
            for (int i = 0; i < Math.min(documents.size(), documentTypes.size()); i++) {
                String docType = documentTypes.get(i).toLowerCase();
                docMap.computeIfAbsent(docType, k -> new ArrayList<>()).add(documents.get(i));
            }
            dto.setDocuments(docMap);
        }
        return registerUser(dto, "MANAGER");
    }

    @PostMapping(value = "/register/user", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registerUser(
            @RequestPart("userData") String userDataStr,
            @RequestParam(value = "documentTypes", required = false) List<String> documentTypes,
            @RequestParam(value = "documents", required = false) List<MultipartFile> documents) throws Exception {

        RegisterDto dto = new ObjectMapper().readValue(userDataStr, RegisterDto.class);
        if (documents != null && documentTypes != null) {
            Map<String, List<MultipartFile>> docMap = new HashMap<>();
            for (int i = 0; i < Math.min(documents.size(), documentTypes.size()); i++) {
                String docType = documentTypes.get(i).toLowerCase();
                docMap.computeIfAbsent(docType, k -> new ArrayList<>()).add(documents.get(i));
            }
            dto.setDocuments(docMap);
        }
        return registerUser(dto, "USER");
    }

    @Transactional
    private ResponseEntity<?> registerUser(RegisterDto dto, String roleName) {
        if (userRepo.existsByUsername(dto.getUsername())) {
            logger.warn("Registration failed: Username {} already exists", dto.getUsername());
            return ResponseUtil.fail("Username already exists", null, HttpStatus.BAD_REQUEST);
        }
        if (dto.getEmail() != null && userRepo.existsByEmail(dto.getEmail())) {
            logger.warn("Registration failed: Email {} already exists", dto.getEmail());
            return ResponseUtil.fail("Email already exists", null, HttpStatus.BAD_REQUEST);
        }

        Map<String, DocumentMaster> docTypes = documentMasterRepository.findAll().stream()
                .collect(Collectors.toMap(DocumentMaster::getDocumentCode, dm -> dm));

        docTypes.forEach((code, doc) ->
                logger.debug("Document type: {} ({}), isMandatory: {}",
                        code, doc.getDocumentName(), doc.isMandatory())
        );

        boolean hasMandatoryDocs = docTypes.values().stream().anyMatch(DocumentMaster::isMandatory);
        if (hasMandatoryDocs) {
            List<String> missingDocs = new ArrayList<>();
            for (DocumentMaster doc : docTypes.values()) {
                List<MultipartFile> files = dto.getDocuments() != null ?
                        dto.getDocuments().get(doc.getDocumentCode()) : null;
                if (doc.isMandatory() && (files == null || files.isEmpty() || files.stream().allMatch(MultipartFile::isEmpty))) {
                    missingDocs.add(doc.getDocumentName());
                }
            }
            if (!missingDocs.isEmpty()) {
                logger.warn("Registration failed for username {}: Mandatory documents missing: {}",
                        dto.getUsername(), String.join(", ", missingDocs));
                return ResponseUtil.fail("Mandatory documents missing: " + String.join(", ", missingDocs),
                        null, HttpStatus.BAD_REQUEST);
            }
        } else {
            logger.debug("No mandatory documents required for registration");
        }

        UserEntity user = new UserEntity();
        user.setUsername(dto.getUsername());
        user.setPassword(encoder.encode(dto.getPassword()));
        user.setEmail(dto.getEmail());

        Role role = roleRepo.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        user.addRole(role);

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            List<UserDocumentDto> documentDtos = new ArrayList<>();
            UserEntity savedUser = userRepo.save(user);

            if (dto.getDocuments() != null) {
                for (Map.Entry<String, List<MultipartFile>> entry : dto.getDocuments().entrySet()) {
                    String docCode = entry.getKey();
                    DocumentMaster docType = docTypes.getOrDefault(docCode, createGenericDocumentMaster(docCode));

                    for (MultipartFile file : entry.getValue()) {
                        if (!file.isEmpty()) {
                            String url = validateAndProcessFileUpload(savedUser, file, docType);

                            UserDocument doc = new UserDocument();
                            doc.setUserId(savedUser.getUserId());
                            doc.setUsername(savedUser.getUsername());
                            doc.setDocumentMaster(docType);
                            doc.setFilePath(url);
                            UserDocument savedDoc = userDocumentRepository.save(doc);

                            documentDtos.add(new UserDocumentDto(
                                    savedDoc.getId(),
                                    docType.getDocumentCode(),
                                    url
                            ));
                        }
                    }
                }
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
                    documentDtos
            );

            logger.info("User registered successfully: {}", savedUser.getUsername());
            return ResponseUtil.success("User registered successfully", responseDto);
        } catch (IOException e) {
            logger.error("Failed to process documents for user {}: {}", dto.getUsername(), e.getMessage());
            return ResponseUtil.fail("Failed to process documents", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private DocumentMaster createGenericDocumentMaster(String docCode) {
        DocumentMaster generic = new DocumentMaster();
        generic.setDocumentCode(docCode);
        generic.setDocumentName("Custom_" + docCode);
        generic.setMandatory(false);
        generic.setAllowedMultiple(true);
        generic.setMaxSize(10.0);
        generic.setAllowedExtensions("jpg,jpeg,png,pdf,doc,docx");
        return generic;
    }

    private String validateAndProcessFileUpload(UserEntity user, MultipartFile file, DocumentMaster docType) throws IOException {
        if (file == null || file.isEmpty()) {
            logger.warn("Empty file uploaded for user {} and document type {}", user.getUsername(), docType.getDocumentCode());
            return null;
        }

        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new IOException("Invalid file: No filename provided");
        }

        String ext = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        String allowedExtensions = docType.getAllowedExtensions() != null ? docType.getAllowedExtensions() : "jpg,jpeg,png,pdf,doc,docx";
        if (!Arrays.asList(allowedExtensions.split(",")).contains(ext)) {
            logger.error("Invalid file extension {} for document type {}", ext, docType.getDocumentName());
            throw new IOException("Invalid file extension for " + docType.getDocumentName() + ": " + ext);
        }

        double sizeMB = file.getSize() / (1024.0 * 1024.0);
        double maxSize = docType.getMaxSize() != null ? docType.getMaxSize() : 10.0;
        if (sizeMB > maxSize) {
            logger.error("File size {}MB exceeds maximum {}MB for document type {}", sizeMB, maxSize, docType.getDocumentName());
            throw new IOException("File exceeds maximum size for " + docType.getDocumentName() + ": " + sizeMB + "MB");
        }

        String userDir = uploadDir + user.getUsername() + "/";
        Files.createDirectories(Paths.get(userDir));

        String newFilename = docType.getDocumentCode() + "_" + System.currentTimeMillis() + "." + ext;
        Path filePath = Paths.get(userDir + newFilename);
        Files.write(filePath, file.getBytes());

        String fileUrl = fileBaseUrl + user.getUsername() + "/" + newFilename;
        logger.info("File uploaded successfully: {} for user {}", fileUrl, user.getUsername());
        return fileUrl;
    }

    private <T> List<T> nonNullList(List<T> list) {
        return list != null ? list : Collections.emptyList();
    }
}