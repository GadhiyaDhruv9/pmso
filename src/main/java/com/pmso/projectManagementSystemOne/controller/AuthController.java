package com.pmso.projectManagementSystemOne.controller;

import com.pmso.projectManagementSystemOne.config.JWTGenerator;
import com.pmso.projectManagementSystemOne.dto.LoginDto;
import com.pmso.projectManagementSystemOne.dto.RegisterDto;
import com.pmso.projectManagementSystemOne.dto.UserResponseDto;
import com.pmso.projectManagementSystemOne.entity.Role;
import com.pmso.projectManagementSystemOne.entity.UserEntity;
import com.pmso.projectManagementSystemOne.repository.RoleRepository;
import com.pmso.projectManagementSystemOne.repository.UserRepository;
import com.pmso.projectManagementSystemOne.utils.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder encoder;
    private final JWTGenerator jwtGen;

    @Autowired
    public AuthController(AuthenticationManager authManager, UserRepository userRepo,
                          RoleRepository roleRepo, PasswordEncoder encoder, JWTGenerator jwtGen) {
        this.authManager = authManager;
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.encoder = encoder;
        this.jwtGen = jwtGen;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDto dto) {
        try {
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getUsernameOrEmail(), dto.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(auth);
            String token = jwtGen.generateToken(auth);
            List<String> roles = auth.getAuthorities().stream()
                    .map(a -> a.getAuthority().replace("ROLE_", ""))
                    .collect(Collectors.toList());

            Map<String, Object> data = Map.of("token", token, "roles", roles);
            return ResponseUtil.success("Login successful", data);
        } catch (BadCredentialsException e) {
            return ResponseUtil.fail("Invalid username or password", e.getMessage(), HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return ResponseUtil.fail("Login failed", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/register/admin")
    public ResponseEntity<?> registerAdmin(@RequestBody RegisterDto dto) {
        return registerUser(dto, "ADMIN");
    }

    @PostMapping("/register/manager")
    public ResponseEntity<?> registerManager(@RequestBody RegisterDto dto) {
        return registerUser(dto, "MANAGER");
    }

    @PostMapping("/register/user")
    public ResponseEntity<?> registerUser(@RequestBody RegisterDto dto) {
        return registerUser(dto, "USER");
    }

    private ResponseEntity<?> registerUser(RegisterDto dto, String roleName) {
        if (userRepo.existsByUsername(dto.getUsername()))
            return ResponseUtil.fail("Username already exists", "Conflict", HttpStatus.BAD_REQUEST);

        if (dto.getEmail() != null && userRepo.existsByEmail(dto.getEmail()))
            return ResponseUtil.fail("Email already exists", "Conflict", HttpStatus.BAD_REQUEST);

        UserEntity user = new UserEntity();
        user.setUsername(dto.getUsername());
        user.setPassword(encoder.encode(dto.getPassword()));
        user.setEmail(dto.getEmail());

        Role role = roleRepo.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        user.addRole(role);
        user.setCreatedBy(null);
        user.setUpdatedBy(null);
        UserEntity savedUser = userRepo.save(user);

        UserResponseDto responseDto = new UserResponseDto(
                savedUser.getUserId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getRoles().stream().map(Role::getName).collect(Collectors.joining(",")),
                savedUser.getPassword(),
                savedUser.getCreatedAt(),
                savedUser.getUpdatedAt(),
                savedUser.getUpdatedBy() != null ? savedUser.getUpdatedBy().getUsername() : null
        );

        return ResponseUtil.success("User registered with role: " + roleName, responseDto);
    }

}