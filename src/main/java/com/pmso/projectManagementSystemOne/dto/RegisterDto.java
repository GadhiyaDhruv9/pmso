package com.pmso.projectManagementSystemOne.dto;

import org.springframework.web.multipart.MultipartFile;
import javax.validation.constraints.*;
import java.util.List;
import java.util.Map;

public class RegisterDto {
    @NotBlank private String username;
    @NotBlank private String password;
    @NotBlank @Email private String email;

    private Map<String, List<MultipartFile>> documents;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Map<String, List<MultipartFile>> getDocuments() { return documents; }
    public void setDocuments(Map<String, List<MultipartFile>> documents) {
        this.documents = documents;
    }
}