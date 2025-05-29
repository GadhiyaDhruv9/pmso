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
            @RequestParam(value = "fileCounts", required = false) List<Integer> fileCounts,
            @RequestParam Map<String, String> allParams,
            @RequestParam(value = "documents", required = false) List<MultipartFile> documents) throws IOException {

        RegisterDto dto = new RegisterDto();
        dto.setUsername(username);
        dto.setPassword(password);
        dto.setEmail(email);

        // Try to parse grouped structure first
        Map<String, List<MultipartFile>> docMap = new LinkedHashMap<>();
        boolean groupedStructureDetected = false;

        for (Map.Entry<String, String> entry : allParams.entrySet()) {
            if (entry.getKey().startsWith("documentTypes[")) {
                groupedStructureDetected = true;
                String docType = entry.getKey().replaceAll("documentTypes\\[(\\w+)\\]\\[\\]", "$1");
                String fileKeyPrefix = "documents[" + docType + "][";
                List<MultipartFile> filesForType = documents != null ? documents.stream()
                        .filter(file -> file.getName() != null && file.getName().startsWith(fileKeyPrefix))
                        .collect(Collectors.toList()) : new ArrayList<>();
                if (!filesForType.isEmpty()) {
                    docMap.put(docType, filesForType);
                }
            }
        }

        // If grouped structure wasn't detected, fall back to flat structure
        if (!groupedStructureDetected && documentTypes != null && documents != null) {
            docMap = pairDocumentsWithTypes(documentTypes, documents, fileCounts);
        }

        dto.setDocuments(docMap);
        return registerUser(dto, "ADMIN");
    }

    @PostMapping(value = "/register/manager", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registerManager(
            @RequestPart("userData") String userDataStr,
            @RequestParam(value = "documentTypes", required = false) List<String> documentTypes,
            @RequestParam(value = "fileCounts", required = false) List<Integer> fileCounts,
            @RequestParam Map<String, String> allParams,
            @RequestParam(value = "documents", required = false) List<MultipartFile> documents) throws IOException {

        RegisterDto dto = new ObjectMapper().readValue(userDataStr, RegisterDto.class);

        // Try to parse grouped structure first
        Map<String, List<MultipartFile>> docMap = new LinkedHashMap<>();
        boolean groupedStructureDetected = false;

        for (Map.Entry<String, String> entry : allParams.entrySet()) {
            if (entry.getKey().startsWith("documentTypes[")) {
                groupedStructureDetected = true;
                String docType = entry.getKey().replaceAll("documentTypes\\[(\\w+)\\]\\[\\]", "$1");
                String fileKeyPrefix = "documents[" + docType + "][";
                List<MultipartFile> filesForType = documents != null ? documents.stream()
                        .filter(file -> file.getName() != null && file.getName().startsWith(fileKeyPrefix))
                        .collect(Collectors.toList()) : new ArrayList<>();
                if (!filesForType.isEmpty()) {
                    docMap.put(docType, filesForType);
                }
            }
        }

        // If grouped structure wasn't detected, fall back to flat structure
        if (!groupedStructureDetected && documentTypes != null && documents != null) {
            docMap = pairDocumentsWithTypes(documentTypes, documents, fileCounts);
        }

        dto.setDocuments(docMap);
        return registerUser(dto, "MANAGER");
    }

    @PostMapping(value = "/register/user", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registerUser(
            @RequestPart("userData") String userDataStr,
            @RequestParam(value = "documentTypes", required = false) List<String> documentTypes,
            @RequestParam(value = "fileCounts", required = false) List<Integer> fileCounts,
            @RequestParam Map<String, String> allParams,
            @RequestParam(value = "documents", required = false) List<MultipartFile> documents) throws IOException {

        RegisterDto dto = new ObjectMapper().readValue(userDataStr, RegisterDto.class);

        // Try to parse grouped structure first
        Map<String, List<MultipartFile>> docMap = new LinkedHashMap<>();
        boolean groupedStructureDetected = false;

        for (Map.Entry<String, String> entry : allParams.entrySet()) {
            if (entry.getKey().startsWith("documentTypes[")) {
                groupedStructureDetected = true;
                String docType = entry.getKey().replaceAll("documentTypes\\[(\\w+)\\]\\[\\]", "$1");
                String fileKeyPrefix = "documents[" + docType + "][";
                List<MultipartFile> filesForType = documents != null ? documents.stream()
                        .filter(file -> file.getName() != null && file.getName().startsWith(fileKeyPrefix))
                        .collect(Collectors.toList()) : new ArrayList<>();
                if (!filesForType.isEmpty()) {
                    docMap.put(docType, filesForType);
                }
            }
        }

        // If grouped structure wasn't detected, fall back to flat structure
        if (!groupedStructureDetected && documentTypes != null && documents != null) {
            docMap = pairDocumentsWithTypes(documentTypes, documents, fileCounts);
        }

        dto.setDocuments(docMap);
        return registerUser(dto, "USER");
    }

    private Map<String, List<MultipartFile>> pairDocumentsWithTypes(List<String> documentTypes, List<MultipartFile> documents, List<Integer> fileCounts) {
        Map<String, List<MultipartFile>> docMap = new LinkedHashMap<>();

        if (documentTypes == null || documents == null || documentTypes.isEmpty() || documents.isEmpty()) {
            return docMap;
        }

        // Clean up documentTypes to remove duplicates while preserving order
        List<String> uniqueDocTypes = new ArrayList<>(new LinkedHashSet<>(documentTypes));
        int fileIndex = 0;

        // If fileCounts is provided and matches the number of document types, use it
        if (fileCounts != null && fileCounts.size() == uniqueDocTypes.size()) {
            for (int i = 0; i < uniqueDocTypes.size(); i++) {
                String docType = uniqueDocTypes.get(i);
                int count = fileCounts.get(i);
                if (docType != null && !docType.isBlank() && count > 0) {
                    List<MultipartFile> filesForType = new ArrayList<>();
                    for (int j = 0; j < count && fileIndex < documents.size(); j++, fileIndex++) {
                        MultipartFile file = documents.get(fileIndex);
                        if (file != null && !file.isEmpty()) {
                            filesForType.add(file);
                        }
                    }
                    if (!filesForType.isEmpty()) {
                        docMap.put(docType, filesForType);
                    }
                }
            }
            return docMap;
        }

        // Fallback: Use a default mapping strategy based on document type expectations
        // Define expected file counts for each document type
        Map<String, Integer> defaultFileCounts = new HashMap<>();
        defaultFileCounts.put("profile", 2);
        defaultFileCounts.put("aadhar", 2);
        defaultFileCounts.put("pan", 2);
        defaultFileCounts.put("address", 1); // Address typically has 1 file
        defaultFileCounts.put("bank", 1);   // Bank typically has 1 file
        defaultFileCounts.put("gst", 2);

        for (String docType : uniqueDocTypes) {
            if (docType != null && !docType.isBlank()) {
                int expectedCount = defaultFileCounts.getOrDefault(docType.toLowerCase(), 1);
                List<MultipartFile> filesForType = new ArrayList<>();
                for (int j = 0; j < expectedCount && fileIndex < documents.size(); j++, fileIndex++) {
                    MultipartFile file = documents.get(fileIndex);
                    if (file != null && !file.isEmpty()) {
                        filesForType.add(file);
                    }
                }
                if (!filesForType.isEmpty()) {
                    docMap.put(docType, filesForType);
                }
            }
        }

        return docMap;
    }

    @Transactional
    private ResponseEntity<?> registerUser(RegisterDto dto, String roleName) {
        try {
            // Validate user existence
            if (userRepo.existsByUsername(dto.getUsername())) {
                return ResponseUtil.fail("Username already exists", null, HttpStatus.BAD_REQUEST);
            }
            if (dto.getEmail() != null && userRepo.existsByEmail(dto.getEmail())) {
                return ResponseUtil.fail("Email already exists", null, HttpStatus.BAD_REQUEST);
            }

            // Get all document types from DB
            Map<String, DocumentMaster> existingDocTypes = documentMasterRepository.findAll()
                    .stream()
                    .collect(Collectors.toMap(DocumentMaster::getDocumentCode, dm -> dm));

            // Validate mandatory documents
            List<String> missingDocs = existingDocTypes.values().stream()
                    .filter(docMaster -> docMaster.isMandatory())
                    .filter(doc -> dto.getDocuments() == null ||
                            !dto.getDocuments().containsKey(doc.getDocumentCode()) ||
                            dto.getDocuments().get(doc.getDocumentCode()).isEmpty())
                    .map(DocumentMaster::getDocumentName)
                    .collect(Collectors.toList());

            if (!missingDocs.isEmpty()) {
                return ResponseUtil.fail("Mandatory documents missing: " + String.join(", ", missingDocs),
                        null, HttpStatus.BAD_REQUEST);
            }

            // Validate document types and files
            if (dto.getDocuments() != null) {
                for (Map.Entry<String, List<MultipartFile>> entry : dto.getDocuments().entrySet()) {
                    String docType = entry.getKey();
                    List<MultipartFile> files = entry.getValue();

                    if (!existingDocTypes.containsKey(docType)) {
                        return ResponseUtil.fail("Invalid document type: " + docType,
                                null, HttpStatus.BAD_REQUEST);
                    }

                    DocumentMaster docMaster = existingDocTypes.get(docType);

                    if (files.size() > 1 && !docMaster.isAllowedMultiple()) {
                        return ResponseUtil.fail("Multiple files not allowed for document type: " + docType,
                                null, HttpStatus.BAD_REQUEST);
                    }
                }
            }

            // Create user
            UserEntity user = new UserEntity();
            user.setUsername(dto.getUsername());
            user.setPassword(encoder.encode(dto.getPassword()));
            user.setEmail(dto.getEmail());

            Role role = roleRepo.findByName(roleName)
                    .orElseThrow(() -> new RuntimeException("Role not found"));
            user.addRole(role);

            // Create upload directory
            Path uploadPath = Paths.get(uploadDir);
            Files.createDirectories(uploadPath);

            // Process documents
            Map<String, List<String>> docTypeToFileUrls = new LinkedHashMap<>();
            UserEntity savedUser = userRepo.save(user);
            List<UserDocument> savedDocuments = new ArrayList<>();

            if (dto.getDocuments() != null) {
                for (Map.Entry<String, List<MultipartFile>> entry : dto.getDocuments().entrySet()) {
                    String docType = entry.getKey();
                    DocumentMaster docMaster = existingDocTypes.get(docType);

                    for (MultipartFile file : entry.getValue()) {
                        try {
                            String url = processFileUpload(savedUser, file, docMaster);

                            UserDocument doc = new UserDocument();
                            doc.setUserId(savedUser.getUserId());
                            doc.setUsername(savedUser.getUsername());
                            doc.setDocumentMaster(docMaster);
                            doc.setFilePath(url);
                            UserDocument savedDoc = userDocumentRepository.save(doc);
                            savedDocuments.add(savedDoc);

                            docTypeToFileUrls.computeIfAbsent(docType, k -> new ArrayList<>()).add(url);
                        } catch (IOException e) {
                            logger.error("Failed to process file for document type {}: {}", docType, e.getMessage());
                            return ResponseUtil.fail("Failed to process file for document type: " + docType,
                                    e.getMessage(), HttpStatus.BAD_REQUEST);
                        }
                    }
                }
            }

            // Prepare response
            List<UserDocumentDto> documentDtos = docTypeToFileUrls.entrySet().stream()
                    .map(entry -> new UserDocumentDto(
                            savedDocuments.stream()
                                    .filter(d -> d.getDocumentMaster().getDocumentCode().equals(entry.getKey()))
                                    .map(UserDocument::getId)
                                    .findFirst()
                                    .orElse(null),
                            entry.getKey(),
                            entry.getValue()
                    ))
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

    private String processFileUpload(UserEntity user, MultipartFile file, DocumentMaster docType) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("Empty file uploaded");
        }

        // Check file extension
        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank()) {
            throw new IOException("Invalid file: No filename provided");
        }

        String ext = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        if (!Arrays.asList(docType.getAllowedExtensions().split(",")).contains(ext)) {
            throw new IOException("Invalid file extension: " + ext +
                    ". Allowed: " + docType.getAllowedExtensions());
        }

        // Check file size
        double sizeMB = file.getSize() / (1024.0 * 1024.0);
        if (docType.getMaxSize() != null && sizeMB > docType.getMaxSize()) {
            throw new IOException("File exceeds maximum size: " + sizeMB + "MB. Max allowed: " +
                    docType.getMaxSize() + "MB");
        }

        // Create user directory
        String userDir = uploadDir + user.getUsername() + "/";
        Files.createDirectories(Paths.get(userDir));

        // Save file with document type name and timestamp to avoid conflicts
        String newFilename = docType.getDocumentCode() + "_" + System.currentTimeMillis() + "." + ext;
        Path filePath = Paths.get(userDir + newFilename);
        Files.write(filePath, file.getBytes());

        return fileBaseUrl + user.getUsername() + "/" + newFilename;
    }
}