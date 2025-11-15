package com.bank.capp.constants;

public class SecurityConstants {
    
    // HTTP Headers
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String API_KEY_HEADER = "X-API-Key";
    public static final String BEARER_PREFIX = "Bearer ";
    
    // Token Settings
    public static final long JWT_EXPIRATION_MS = 3600000; // 1 hour
    public static final long REFRESH_TOKEN_EXPIRATION_MS = 604800000; // 7 days
    
    // Roles
    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_OPERATOR = "ROLE_OPERATOR";
    public static final String ROLE_VIEWER = "ROLE_VIEWER";
    public static final String ROLE_SERVICE = "ROLE_SERVICE";
    
    // API Endpoints
    public static final String AUTH_BASE_PATH = "/api/v1/auth";
    public static final String SERVICE_ACCOUNT_BASE_PATH = "/api/v1/service-accounts";
    
    // Public Endpoints (No Authentication Required)
    public static final String[] PUBLIC_ENDPOINTS = {
        AUTH_BASE_PATH + "/**",
        "/actuator/health",
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/swagger-ui.html"
    };
    
    // Messages
    public static final String INVALID_CREDENTIALS = "Invalid username or password";
    public static final String USER_NOT_FOUND = "User not found";
    public static final String TOKEN_EXPIRED = "Token has expired";
    public static final String INVALID_TOKEN = "Invalid token";
    public static final String API_KEY_INVALID = "Invalid API key";
    public static final String UNAUTHORIZED_ACCESS = "Unauthorized access";
    
    private SecurityConstants() {
        // Private constructor to prevent instantiation
    }
}
