package com.pmso.projectManagementSystemOne.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

public class UserResponseDto {
    private Long userId;
    private String username;
    private String email;
    private String roleNames;
    private String password;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String updatedByUsername;
    private List<UserDocumentDto> documents;

    public UserResponseDto() {}

    public UserResponseDto(Long userId, String username, String email, String roleNames,
                           String password, LocalDateTime createdAt, LocalDateTime updatedAt,
                           String updatedByUsername, List<UserDocumentDto> documents) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.roleNames = roleNames;
        this.password = password;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.updatedByUsername = updatedByUsername;
        this.documents = documents;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRoleNames() { return roleNames; }
    public void setRoleNames(String roleNames) { this.roleNames = roleNames; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUpdatedByUsername() { return updatedByUsername; }
    public void setUpdatedByUsername(String updatedByUsername) { this.updatedByUsername = updatedByUsername; }

    public List<UserDocumentDto> getDocuments() { return documents; }
    public void setDocuments(List<UserDocumentDto> documents) { this.documents = documents; }
}