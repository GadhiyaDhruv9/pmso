package com.pmso.projectManagementSystemOne.config;

import com.pmso.projectManagementSystemOne.Service.CustomUserDetailService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JWTAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JWTAuthenticationFilter.class);
    private final JWTGenerator tokenGenerator;
    private final CustomUserDetailService customUserDetailService;

    public JWTAuthenticationFilter(JWTGenerator tokenGenerator, CustomUserDetailService customUserDetailService) {
        this.tokenGenerator = tokenGenerator;
        this.customUserDetailService = customUserDetailService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String token = getJWTFromRequest(request);
        log.info("Received token: {}", token);
        log.info("Request URI: {}", request.getRequestURI());
        log.info("Authorization header: {}", request.getHeader("Authorization"));

        if (StringUtils.hasText(token)) {
            try {
                if (tokenGenerator.validateToken(token)) {
                    String username = tokenGenerator.getUsernameFromJWT(token);
                    log.info("Extracted username from token: {}", username);
                    UserDetails userDetails = customUserDetailService.loadUserByUsername(username);
                    log.info("Loaded user details: {}", userDetails.getUsername());
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    log.info("Security context set for user: {}", username);
                } else {
                    log.warn("Token validation failed: Token is invalid or expired");
                }
            } catch (Exception e) {
                log.error("Token validation exception: {}", e.getMessage(), e);
            }
        } else {
            log.warn("No token found in request");
        }
        filterChain.doFilter(request, response);
    }

    private String getJWTFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/auth/");
    }
}