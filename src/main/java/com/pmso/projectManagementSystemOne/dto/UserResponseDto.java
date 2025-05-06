package com.pmso.projectManagementSystemOne.dto;

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

    // Changed from single String to List<String>
    private List<String> profilePicturePaths;
    private List<String> panCardPaths;
    private List<String> aadharCardPaths;
    private List<String> proofOfAddressPaths;
    private List<String> bankDetailsPaths;

    public UserResponseDto(Long userId, String username, String email, String roleNames,
                           String password, LocalDateTime createdAt, LocalDateTime updatedAt,
                           String updatedByUsername,
                           List<String> profilePicturePaths,
                           List<String> panCardPaths,
                           List<String> aadharCardPaths,
                           List<String> proofOfAddressPaths,
                           List<String> bankDetailsPaths) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.roleNames = roleNames;
        this.password = password;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.updatedByUsername = updatedByUsername;
        this.profilePicturePaths = profilePicturePaths;
        this.panCardPaths = panCardPaths;
        this.aadharCardPaths = aadharCardPaths;
        this.proofOfAddressPaths = proofOfAddressPaths;
        this.bankDetailsPaths = bankDetailsPaths;
    }

    // Getters
    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getRoleNames() { return roleNames; }
    public String getPassword() { return password; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public String getUpdatedByUsername() { return updatedByUsername; }
    public List<String> getProfilePicturePaths() { return profilePicturePaths; }
    public List<String> getPanCardPaths() { return panCardPaths; }
    public List<String> getAadharCardPaths() { return aadharCardPaths; }
    public List<String> getProofOfAddressPaths() { return proofOfAddressPaths; }
    public List<String> getBankDetailsPaths() { return bankDetailsPaths; }

    // Setters
    public void setUserId(Long userId) { this.userId = userId; }
    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setRoleNames(String roleNames) { this.roleNames = roleNames; }
    public void setPassword(String password) { this.password = password; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setUpdatedByUsername(String updatedByUsername) { this.updatedByUsername = updatedByUsername; }
    public void setProfilePicturePaths(List<String> profilePicturePaths) { this.profilePicturePaths = profilePicturePaths; }
    public void setPanCardPaths(List<String> panCardPaths) { this.panCardPaths = panCardPaths; }
    public void setAadharCardPaths(List<String> aadharCardPaths) { this.aadharCardPaths = aadharCardPaths; }
    public void setProofOfAddressPaths(List<String> proofOfAddressPaths) { this.proofOfAddressPaths = proofOfAddressPaths; }
    public void setBankDetailsPaths(List<String> bankDetailsPaths) { this.bankDetailsPaths = bankDetailsPaths; }
}