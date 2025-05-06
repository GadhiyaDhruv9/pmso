package com.pmso.projectManagementSystemOne.dto;

import org.springframework.web.multipart.MultipartFile;
import javax.validation.constraints.*;
import java.util.List;

public class RegisterDto {
    @NotBlank private String username;
    @NotBlank private String password;
    @NotBlank @Email private String email;

    private List<MultipartFile> profilePictures;
    private List<MultipartFile> panCards;
    private List<MultipartFile> aadharCards;
    private List<MultipartFile> proofOfAddresses;
    private List<MultipartFile> bankDetails;

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public List<MultipartFile> getProfilePictures() { return profilePictures; }
    public void setProfilePictures(List<MultipartFile> profilePictures) {
        this.profilePictures = profilePictures;
    }

    public List<MultipartFile> getPanCards() { return panCards; }
    public void setPanCards(List<MultipartFile> panCards) {
        this.panCards = panCards;
    }

    public List<MultipartFile> getAadharCards() { return aadharCards; }
    public void setAadharCards(List<MultipartFile> aadharCards) {
        this.aadharCards = aadharCards;
    }

    public List<MultipartFile> getProofOfAddresses() { return proofOfAddresses; }
    public void setProofOfAddresses(List<MultipartFile> proofOfAddresses) {
        this.proofOfAddresses = proofOfAddresses;
    }

    public List<MultipartFile> getBankDetails() { return bankDetails; }
    public void setBankDetails(List<MultipartFile> bankDetails) {
        this.bankDetails = bankDetails;
    }
}