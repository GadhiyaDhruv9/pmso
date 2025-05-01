package com.pmso.projectManagementSystemOne.dto;

import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

public class RegisterDto {
    @NotBlank
    private String username;

    @NotBlank
    private String password;

    @NotBlank
    @Email
    private String email;

    private MultipartFile profilePicture;
    private MultipartFile panCard;
    private MultipartFile aadharCard;
    private MultipartFile proofOfAddress;
    private MultipartFile bankDetails;

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public MultipartFile getProfilePicture() { return profilePicture; }
    public void setProfilePicture(MultipartFile profilePicture) { this.profilePicture = profilePicture; }

    public MultipartFile getPanCard() { return panCard; }
    public void setPanCard(MultipartFile panCard) { this.panCard = panCard; }

    public MultipartFile getAadharCard() { return aadharCard; }
    public void setAadharCard(MultipartFile aadharCard) { this.aadharCard = aadharCard; }

    public MultipartFile getProofOfAddress() { return proofOfAddress; }
    public void setProofOfAddress(MultipartFile proofOfAddress) { this.proofOfAddress = proofOfAddress; }

    public MultipartFile getBankDetails() { return bankDetails; }
    public void setBankDetails(MultipartFile bankDetails) { this.bankDetails = bankDetails; }
}