package com.pmso.projectManagementSystemOne.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class RefreshTokenDto {
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}