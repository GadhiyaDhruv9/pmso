package com.pmso.projectManagementSystemOne.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "token_blacklist")
@Getter
@Setter
@NoArgsConstructor
public class TokenBlacklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private Instant expiryDate;

    public TokenBlacklist(String token, Instant expiryDate) {
        this.token = token;
        this.expiryDate = expiryDate;
    }
}