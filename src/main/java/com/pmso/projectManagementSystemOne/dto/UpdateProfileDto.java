package com.pmso.projectManagementSystemOne.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

@Getter
@Setter
public class UpdateProfileDto {
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @Email(message = "Email should be valid")
    private String email;

    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
}