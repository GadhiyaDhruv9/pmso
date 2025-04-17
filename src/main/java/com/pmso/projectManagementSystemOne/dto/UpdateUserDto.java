package com.pmso.projectManagementSystemOne.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

@Data
public class UpdateUserDto {
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @Email(message = "Email should be valid")
    private String email;
}