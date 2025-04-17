package com.pmso.projectManagementSystemOne.utils;

import com.pmso.projectManagementSystemOne.exception.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseUtil {

    public static <T> ResponseEntity<ApiResponse<T>> success(String message, T data) {
        return new ResponseEntity<>(new ApiResponse<>(true, message, data), HttpStatus.OK);
    }

    public static <T> ResponseEntity<ApiResponse<T>> created(String message, T data) {
        return new ResponseEntity<>(new ApiResponse<>(true, message, data), HttpStatus.CREATED);
    }

    public static <T> ResponseEntity<ApiResponse<T>> fail(String msg, String error, HttpStatus status) {
        return new ResponseEntity<>(new ApiResponse<>(false, msg, error), status);
    }

}
