package com.bank.capp.services;

import com.bank.capp.constants.ApiConstants;
import com.bank.capp.models.*;
import com.bank.capp.repository.RefreshTokenRepository;
import com.bank.capp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * Register a new user
     */
    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException(ApiConstants.USERNAME_EXISTS);
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException(ApiConstants.EMAIL_EXISTS);
        }

        // Create new user
        var user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(Role.USER)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();

        var savedUser = userRepository.save(user);
        log.info("New user registered: {}", savedUser.getUsername());

        // Generate tokens
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        // Save refresh token
        saveRefreshToken(user, refreshToken);

        return buildAuthenticationResponse(savedUser, jwtToken, refreshToken);
    }

    /**
     * Authenticate user and generate tokens
     */
    @Transactional
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        // Authenticate user
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // Fetch user
        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException(ApiConstants.USER_NOT_FOUND));

        // Update last login
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // Generate tokens
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        // Revoke old refresh tokens and save new one
        revokeAllUserTokens(user);
        saveRefreshToken(user, refreshToken);

        log.info("User authenticated: {}", user.getUsername());

        return buildAuthenticationResponse(user, jwtToken, refreshToken);
    }

    /**
     * Refresh access token using refresh token
     */
    @Transactional
    public AuthenticationResponse refreshToken(RefreshTokenRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        // Find refresh token
        RefreshToken refreshToken = refreshTokenRepository.findByToken(requestRefreshToken)
                .orElseThrow(() -> new IllegalArgumentException(ApiConstants.INVALID_REFRESH_TOKEN));

        // Validate refresh token
        if (refreshToken.isRevoked()) {
            throw new IllegalArgumentException(ApiConstants.REFRESH_TOKEN_REVOKED);
        }

        if (refreshToken.isExpired()) {
            throw new IllegalArgumentException(ApiConstants.REFRESH_TOKEN_EXPIRED);
        }

        // Get user
        User user = refreshToken.getUser();

        // Validate JWT in refresh token
        if (!jwtService.isTokenValid(requestRefreshToken, user)) {
            throw new IllegalArgumentException(ApiConstants.INVALID_REFRESH_TOKEN);
        }

        // Generate new access token
        String accessToken = jwtService.generateToken(user);

        log.info("Token refreshed for user: {}", user.getUsername());

        return buildAuthenticationResponse(user, accessToken, requestRefreshToken);
    }

    /**
     * Logout user by revoking refresh token
     */
    @Transactional
    public void logout(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new IllegalArgumentException(ApiConstants.INVALID_REFRESH_TOKEN));

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        log.info("User logged out: {}", refreshToken.getUser().getUsername());
    }

    /**
     * Save refresh token to database
     */
    private void saveRefreshToken(User user, String token) {
        long refreshExpirationSeconds = 604800; // 7 days - can be moved to config
        var refreshToken = RefreshToken.builder()
                .user(user)
                .token(token)
                .expiryDate(LocalDateTime.now().plusSeconds(refreshExpirationSeconds))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);
    }

    /**
     * Revoke all refresh tokens for a user
     */
    private void revokeAllUserTokens(User user) {
        refreshTokenRepository.findByUser(user).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }

    /**
     * Build authentication response
     */
    private AuthenticationResponse buildAuthenticationResponse(
            User user,
            String accessToken,
            String refreshToken
    ) {
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType(ApiConstants.TOKEN_TYPE_BEARER)
                .expiresIn(jwtService.getJwtExpirationInSeconds())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }
}
