package com.pmso.projectManagementSystemOne.dto;

import java.time.LocalDateTime;

public class UserResponseDto {
    private Long userId;
    private String username;
    private String email;
    private String roleNames;
    private String password;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String updatedByUsername;
    private String profilePicturePath;
    private String panCardPath;
    private String aadharCardPath;
    private String proofOfAddressPath;
    private String bankDetailsPath;

    public UserResponseDto(Long userId, String username, String email, String roleNames, String password,
                           LocalDateTime createdAt, LocalDateTime updatedAt, String updatedByUsername,
                           String profilePicturePath, String panCardPath, String aadharCardPath,
                           String proofOfAddressPath, String bankDetailsPath) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.roleNames = roleNames;
        this.password = password;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.updatedByUsername = updatedByUsername;
        this.profilePicturePath = profilePicturePath;
        this.panCardPath = panCardPath;
        this.aadharCardPath = aadharCardPath;
        this.proofOfAddressPath = proofOfAddressPath;
        this.bankDetailsPath = bankDetailsPath;
    }

    // Getters and setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRoleNames() { return roleNames; }
    public void setRoleNames(String roleNames) { this.roleNames = roleNames; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUpdatedByUsername() { return updatedByUsername; }
    public void setUpdatedByUsername(String updatedByUsername) { this.updatedByUsername = updatedByUsername; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getProfilePicturePath() { return profilePicturePath; }
    public void setProfilePicturePath(String profilePicturePath) { this.profilePicturePath = profilePicturePath; }

    public String getPanCardPath() { return panCardPath; }
    public void setPanCardPath(String panCardPath) { this.panCardPath = panCardPath; }

    public String getAadharCardPath() { return aadharCardPath; }
    public void setAadharCardPath(String aadharCardPath) { this.aadharCardPath = aadharCardPath; }

    public String getProofOfAddressPath() { return proofOfAddressPath; }
    public void setProofOfAddressPath(String proofOfAddressPath) { this.proofOfAddressPath = proofOfAddressPath; }

    public String getBankDetailsPath() { return bankDetailsPath; }
    public void setBankDetailsPath(String bankDetailsPath) { this.bankDetailsPath = bankDetailsPath; }
}