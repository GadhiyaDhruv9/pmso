package com.pmso.projectManagementSystemOne.controller;

import com.pmso.projectManagementSystemOne.dto.UpdateUserProfileDto;
import com.pmso.projectManagementSystemOne.dto.UserResponseDto;
import com.pmso.projectManagementSystemOne.dto.userOnlyDto;
import com.pmso.projectManagementSystemOne.entity.DocumentMaster;
import com.pmso.projectManagementSystemOne.entity.Role;
import com.pmso.projectManagementSystemOne.entity.UserDocument;
import com.pmso.projectManagementSystemOne.entity.UserEntity;
import com.pmso.projectManagementSystemOne.repository.DocumentMasterRepository;
import com.pmso.projectManagementSystemOne.repository.UserDocumentRepository;
import com.pmso.projectManagementSystemOne.repository.UserRepository;
import com.pmso.projectManagementSystemOne.utils.CommonUtil;
import com.pmso.projectManagementSystemOne.utils.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/users")
public class UserController extends CommonUtil {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final UserDocumentRepository userDocumentRepository;
    private final DocumentMasterRepository documentMasterRepository;

    @Autowired
    public UserController(UserRepository userRepo, PasswordEncoder encoder,
                          UserDocumentRepository userDocumentRepository, DocumentMasterRepository documentMasterRepository) {
        this.userRepo = userRepo;
        this.encoder = encoder;
        this.userDocumentRepository = userDocumentRepository;
        this.documentMasterRepository = documentMasterRepository;
    }

    //UPDATE USER PROFILE
    @PutMapping("/profile-update")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUserProfile(Authentication auth, @Valid @RequestBody UpdateUserProfileDto dto) {
        try {
            String username = auth.getName();
            UserEntity user = userRepo.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found."));

            if (dto.getUsername() != null && !dto.getUsername().isBlank() && !dto.getUsername().equals(username)) {
                if (userRepo.existsByUsername(dto.getUsername())) {
                    logger.warn("Profile update failed, username={}, error= Username taken", username);
                    return ResponseUtil.fail("Username already taken", null, HttpStatus.BAD_REQUEST);
                }
                user.setUsername(dto.getUsername());
            }

            if (dto.getEmail() != null && !dto.getEmail().isBlank() && !dto.getEmail().equals(user.getEmail())) {
                if (userRepo.existsByEmail(dto.getEmail())) {
                    logger.warn("Profile updated failed, username={}, error = Email taken", username);
                    return ResponseUtil.fail("Email already taken", null, HttpStatus.BAD_REQUEST);
                }
                user.setEmail(dto.getEmail());
            }

            if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
                user.setPassword(encoder.encode(dto.getPassword()));
            }

            UserEntity updatedUser = userRepo.save(user);
            List<UserDocument> documents = userDocumentRepository.findByUserId(updatedUser.getUserId());

            UserResponseDto userResponseDto = createUserResponseDto(updatedUser, documents);
            logger.info("Profile updated, username={}", updatedUser.getUsername());
            return ResponseUtil.success("Profile updated", userResponseDto);
        } catch (Exception e) {
            logger.error("Profile update error, username={}, error={}", auth.getName(), e.getMessage());
            return ResponseUtil.fail("Failed to update profile", null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private UserResponseDto createUserResponseDto(UserEntity user, List<UserDocument> documents) {
        Map<String, List<String>> documentPaths = documents.stream()
                .collect(Collectors.groupingBy(
                        doc -> doc.getDocumentMaster().getDocumentCode(),
                        Collectors.mapping(UserDocument::getFilePath, Collectors.toList())
                ));

        return new UserResponseDto(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getRoles().stream().map(Role::getName).collect(Collectors.joining(",")),
                user.getPassword(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getUpdatedBy() != null ? user.getUpdatedBy().getUsername() : null,
                documentPaths.getOrDefault("profile", Collections.emptyList()),
                documentPaths.getOrDefault("pan", Collections.emptyList()),
                documentPaths.getOrDefault("aadhar", Collections.emptyList()),
                documentPaths.getOrDefault("address", Collections.emptyList()),
                documentPaths.getOrDefault("bank", Collections.emptyList())
        );
    }

    //GET REGISTER USERS ONLY NOT ADMINS
    @GetMapping("/non-admin-users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getNonAdminUsers() {
        try {
            logger.info("Fetching users with only USER role");

            List<userOnlyDto> nonAdminUsers = userRepo.findAll().stream()
                    .filter(user -> user.getRoles().stream()
                            .map(Role::getName)
                            .collect(Collectors.toList())
                                .contains("USER") &&
                            !user.getRoles().stream()
                                    .map(Role::getName)
                                    .collect(Collectors.toList())
                                    .contains("ADMIN"))
                    .map(user -> new userOnlyDto(
                            user.getUserId(),
                            user.getUsername()
                    ))
                    .collect(Collectors.toList());

            logger.info("Successfully retrieved {} non-admin users", nonAdminUsers.size());
            return ResponseUtil.success("Non-admin users retrieved successfully", nonAdminUsers);
        } catch (Exception e) {
            logger.error("Error fetching non-admin users: {}", e.getMessage());
            return ResponseUtil.fail("Failed to fetch non-admin users: " + e.getMessage(),
                    null,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}