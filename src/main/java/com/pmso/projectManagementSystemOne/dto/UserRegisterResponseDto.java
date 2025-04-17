package com.pmso.projectManagementSystemOne.dto;

import java.time.LocalDateTime;

public class UserRegisterResponseDto {
    private Long userId;
    private String username;
    private String email;
    private String roleNames;
    private LocalDateTime createdAt;

    public UserRegisterResponseDto(Long userId, String username, String email, String roleNames, LocalDateTime createdAt) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.roleNames = roleNames;
        this.createdAt = createdAt;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRoleNames() {
        return roleNames;
    }

    public void setRoleNames(String roleNames) {
        this.roleNames = roleNames;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
