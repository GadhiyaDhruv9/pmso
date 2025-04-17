package com.pmso.projectManagementSystemOne.exception;

public class UnauthorizedTaskAssignmentException extends RuntimeException {
    public UnauthorizedTaskAssignmentException(String message) {
        super(message);
    }
}