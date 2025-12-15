package com.bank.capp.services;

import com.bank.capp.constants.ApiConstants;
import com.bank.capp.constants.SecurityConstants;
import com.bank.capp.models.*;
import com.bank.capp.repository.ServiceAccountRefreshTokenRepository;
import com.bank.capp.repository.ServiceAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceAccountAuthenticationService {

    private final ServiceAccountRepository serviceAccountRepository;
    private final ServiceAccountRefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * Authenticate service account with API key and return JWT tokens
     */
    @Transactional
    public ServiceAccountAuthenticationResponse authenticate(ServiceAccountLoginRequest request) {
        String serviceName = request.getServiceName();
        String apiKey = request.getApiKey();

        log.info("Service account login attempt for: {}", serviceName);

        // Find service account by name
        ServiceAccount serviceAccount = serviceAccountRepository.findByServiceName(serviceName)
                .orElseThrow(() -> new IllegalArgumentException("Invalid service credentials"));

        // Check if service account is active
        if (!serviceAccount.isActive()) {
            log.warn("Inactive service account login attempt: {}", serviceName);
            throw new IllegalArgumentException("Service account is inactive");
        }

        // Validate API key
        if (!passwordEncoder.matches(apiKey, serviceAccount.getApiKeyHash())) {
            log.warn("Invalid API key for service: {}", serviceName);
            throw new IllegalArgumentException("Invalid service credentials");
        }

        // Update last used timestamp
        serviceAccount.setLastUsedAt(LocalDateTime.now());
        serviceAccountRepository.save(serviceAccount);

        // Generate JWT tokens with service account claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("service_name", serviceName);
        claims.put("service_id", serviceAccount.getId());
        claims.put("type", "service");

        // Create a pseudo UserDetails for JWT generation
        ServiceAccountUserDetails userDetails = new ServiceAccountUserDetails(serviceAccount);
        
        String accessToken = jwtService.generateToken(claims, userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        // Revoke old refresh tokens and save new one
        revokeAllServiceAccountTokens(serviceAccount);
        saveRefreshToken(serviceAccount, refreshToken);

        log.info("Service account authenticated successfully: {}", serviceName);

        return buildAuthenticationResponse(serviceAccount, accessToken, refreshToken);
    }

    /**
     * Refresh access token using refresh token
     */
    @Transactional
    public ServiceAccountAuthenticationResponse refreshToken(ServiceAccountRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        // Find refresh token
        ServiceAccountRefreshToken refreshToken = refreshTokenRepository.findByToken(requestRefreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        // Validate refresh token
        if (refreshToken.isRevoked()) {
            throw new IllegalArgumentException("Refresh token has been revoked");
        }

        if (refreshToken.isExpired()) {
            throw new IllegalArgumentException("Refresh token has expired");
        }

        // Get service account
        ServiceAccount serviceAccount = refreshToken.getServiceAccount();

        // Check if service account is still active
        if (!serviceAccount.isActive()) {
            throw new IllegalArgumentException("Service account is inactive");
        }

        // Generate new access token
        Map<String, Object> claims = new HashMap<>();
        claims.put("service_name", serviceAccount.getServiceName());
        claims.put("service_id", serviceAccount.getId());
        claims.put("type", "service");

        ServiceAccountUserDetails userDetails = new ServiceAccountUserDetails(serviceAccount);
        String accessToken = jwtService.generateToken(claims, userDetails);

        log.info("Service account token refreshed: {}", serviceAccount.getServiceName());

        return buildAuthenticationResponse(serviceAccount, accessToken, requestRefreshToken);
    }

    /**
     * Logout service account by revoking refresh token
     */
    @Transactional
    public void logout(ServiceAccountRefreshRequest request) {
        ServiceAccountRefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        log.info("Service account logged out: {}", refreshToken.getServiceAccount().getServiceName());
    }

    /**
     * Save refresh token to database
     */
    private void saveRefreshToken(ServiceAccount serviceAccount, String token) {
        long refreshExpirationSeconds = 604800; // 7 days
        ServiceAccountRefreshToken refreshToken = ServiceAccountRefreshToken.builder()
                .serviceAccount(serviceAccount)
                .token(token)
                .expiryDate(LocalDateTime.now().plusSeconds(refreshExpirationSeconds))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);
    }

    /**
     * Revoke all refresh tokens for a service account
     */
    private void revokeAllServiceAccountTokens(ServiceAccount serviceAccount) {
        refreshTokenRepository.findByServiceAccount(serviceAccount).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }

    /**
     * Build authentication response
     */
    private ServiceAccountAuthenticationResponse buildAuthenticationResponse(
            ServiceAccount serviceAccount,
            String accessToken,
            String refreshToken
    ) {
        return ServiceAccountAuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType(ApiConstants.TOKEN_TYPE_BEARER)
                .expiresIn(jwtService.getJwtExpirationInSeconds())
                .serviceName(serviceAccount.getServiceName())
                .build();
    }

    /**
     * Inner class to adapt ServiceAccount to UserDetails for JWT generation
     */
    private static class ServiceAccountUserDetails implements org.springframework.security.core.userdetails.UserDetails {
        private final ServiceAccount serviceAccount;

        public ServiceAccountUserDetails(ServiceAccount serviceAccount) {
            this.serviceAccount = serviceAccount;
        }

        @Override
        public String getUsername() {
            return serviceAccount.getServiceName();
        }

        @Override
        public String getPassword() {
            return serviceAccount.getApiKeyHash();
        }

        @Override
        public java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities() {
            return List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority(SecurityConstants.ROLE_SERVICE));
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return serviceAccount.isActive();
        }
    }
}
