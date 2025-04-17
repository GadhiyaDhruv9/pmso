package com.pmso.projectManagementSystemOne.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserProfileDto {
    private String username;
    private String email;
    private String password;

}
