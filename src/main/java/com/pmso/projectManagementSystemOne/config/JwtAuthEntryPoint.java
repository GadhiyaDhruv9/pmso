package com.pmso.projectManagementSystemOne.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("status", "failed");
        errorDetails.put("message", "Authentication failed: Invalid or expired token");
        errorDetails.put("errorType", authException.getClass().getSimpleName());
        errorDetails.put("details", authException.getMessage());

        response.getWriter().write(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(errorDetails));
    }
}


