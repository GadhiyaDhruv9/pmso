package com.pmso.projectManagementSystemOne.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JWTGenerator {
    private final String SECRET_KEY = "your-secret-key";
    private static final Logger logger = LoggerFactory.getLogger(JWTGenerator.class);

    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
        Date currentDate = new Date();
        Date expireDate = new Date(currentDate.getTime() + SecurityConstants.JWT_EXPIRATION);

        String token = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(expireDate)
                .signWith(SignatureAlgorithm.HS256, SecurityConstants.JWT_SECRET)
                .compact();
        logger.info("Generated token for user {}: {}", username, token);
        return token;
    }

    public String getUsernameFromJWT(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(SecurityConstants.JWT_SECRET)
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(SecurityConstants.JWT_SECRET).parseClaimsJws(token);
            logger.info("Token validated successfully");
            return true;
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
            throw new AuthenticationCredentialsNotFoundException("JWT token is expired", e);
        } catch (SignatureException e) {
            logger.error("JWT signature is invalid: {}", e.getMessage());
            throw new AuthenticationCredentialsNotFoundException("JWT signature is invalid", e);
        } catch (MalformedJwtException e) {
            logger.error("JWT token is malformed: {}", e.getMessage());
            throw new AuthenticationCredentialsNotFoundException("JWT token is malformed", e);
        } catch (Exception e) {
            logger.error("JWT validation failed: {}", e.getMessage());
            throw new AuthenticationCredentialsNotFoundException("JWT validation failed", e);
        }
    }
}