package com.pmso.projectManagementSystemOne.controller;

import com.pmso.projectManagementSystemOne.dto.UpdateUserProfileDto;
import com.pmso.projectManagementSystemOne.dto.UserResponseDto;
import com.pmso.projectManagementSystemOne.entity.Role;
import com.pmso.projectManagementSystemOne.entity.UserEntity;
import com.pmso.projectManagementSystemOne.repository.UserRepository;
import com.pmso.projectManagementSystemOne.utils.CommonUtil;
import com.pmso.projectManagementSystemOne.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/users")
public class UserController extends CommonUtil {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;

    @Autowired
    public UserController(UserRepository userRepo, PasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.encoder = encoder;
    }

    //UPDATE USER PROFILE
    @PutMapping("/profile-update")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUserProfile(Authentication auth, @Valid @RequestBody UpdateUserProfileDto dto) {
        try {
            String username = auth.getName();
            UserEntity user = userRepo.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found."));
            // UPDATE USERNAME
            if (dto.getUsername() != null && !dto.getUsername().isBlank() && !dto.getUsername().equals(username)) {
                if (userRepo.existsByUsername(dto.getUsername())) {
                    logger.warn("Profile update failed, username={}, error= Username taken", username);
                    return ResponseUtil.fail("Username already taken", null, HttpStatus.BAD_REQUEST);
                }
                user.setUsername(dto.getUsername());
            }
            // UPDATE EMAIL
            if (dto.getEmail() != null && !dto.getEmail().isBlank() && !dto.getEmail().equals(user.getEmail())) {
                if (userRepo.existsByEmail(dto.getEmail())) {
                    logger.warn("Profile updated failed, username={}, error = Email taken", username);
                    return ResponseUtil.fail("Email already taken", null, HttpStatus.BAD_REQUEST);
                }
                user.setEmail(dto.getEmail());
            }
            // UPDATE PASSWORD
            if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
                user.setPassword(encoder.encode(dto.getPassword()));
            }

            UserEntity updatedUser =userRepo.save(user);
            UserResponseDto userResponseDto = new UserResponseDto(
                    updatedUser.getUserId(),
                    updatedUser.getUsername(),
                    updatedUser.getEmail(),
                    updatedUser.getPassword(),
                    updatedUser.getRoles().stream().map(Role::getName).collect(Collectors.joining(",")),
                    updatedUser.getCreatedAt(),
                    updatedUser.getUpdatedAt(),
                    updatedUser.getUpdatedBy() != null ? updatedUser.getUpdatedBy().getUsername() : null
            );
            logger.info("Profile updated, username={}", updatedUser.getUsername());
            return ResponseUtil.success("Profile updated", userResponseDto);
        } catch (Exception e) {
            logger.error("Profile update error, username={}, error={}", auth.getName(), e.getMessage());
            return ResponseUtil.fail("Failed to update profile", null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
