package com.pmso.projectManagementSystemOne.exception;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private String error;

    // Success Response
    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.error = null;
    }

    // Error Response
    public ApiResponse(boolean success, String message, String error) {
        this.success = success;
        this.message = message;
        this.data = null;
        this.error = error;
    }
}
