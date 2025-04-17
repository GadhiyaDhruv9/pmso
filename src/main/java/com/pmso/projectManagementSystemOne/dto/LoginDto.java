package com.pmso.projectManagementSystemOne.dto;

public class LoginDto {
    private String usernameOrEmail;
    private String password;


    public String getUsernameOrEmail() {
        return usernameOrEmail;
    }

    public String getPassword() {
        return password;
    }
}