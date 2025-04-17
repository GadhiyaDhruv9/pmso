package com.pmso.projectManagementSystemOne.controller;

import com.pmso.projectManagementSystemOne.utils.ResponseUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class TestController {

@GetMapping("/protected")
public ResponseEntity<?> protectedEndpoint(Authentication authentication) {
    if (authentication != null && authentication.isAuthenticated()) {
        Map<String, Object> data = new HashMap<>();
        data.put("username", authentication.getName());
        data.put("roles", authentication.getAuthorities());
        return ResponseUtil.success("Access granted to protected endpoint.", data);
    }

    return ResponseUtil.fail("Access denied: not authenticated.","AUTH_FAILURE", HttpStatus.UNAUTHORIZED);
}

}