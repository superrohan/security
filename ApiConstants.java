package com.bank.capp.constants;

public class ApiConstants {

    // API Versioning
    public static final String API_VERSION = "v1";
    public static final String API_BASE_PATH = "/api/" + API_VERSION;

    // Response Messages
    public static final String SUCCESS_MESSAGE = "Operation completed successfully";
    public static final String REGISTRATION_SUCCESS = "User registered successfully";
    public static final String LOGIN_SUCCESS = "Login successful";
    public static final String LOGOUT_SUCCESS = "Successfully logged out";
    public static final String TOKEN_REFRESH_SUCCESS = "Token refreshed successfully";
    public static final String API_KEY_GENERATED = "API key generated successfully. Please store it securely as it won't be shown again.";
    public static final String API_KEY_REVOKED = "API key revoked successfully";
    public static final String API_KEY_ROTATED = "API key rotated successfully. Old key is now revoked.";
    public static final String TOKEN_VALID = "Token is valid";

    // Error Messages
    public static final String USERNAME_EXISTS = "Username already exists";
    public static final String EMAIL_EXISTS = "Email already exists";
    public static final String INVALID_REFRESH_TOKEN = "Invalid refresh token";
    public static final String REFRESH_TOKEN_REVOKED = "Refresh token has been revoked";
    public static final String REFRESH_TOKEN_EXPIRED = "Refresh token has expired";
    public static final String SERVICE_ACCOUNT_NOT_FOUND = "Service account not found";

    // Validation Messages
    public static final String USERNAME_REQUIRED = "Username is required";
    public static final String EMAIL_REQUIRED = "Email is required";
    public static final String EMAIL_INVALID = "Email should be valid";
    public static final String PASSWORD_REQUIRED = "Password is required";
    public static final String PASSWORD_MIN_LENGTH = "Password must be at least 8 characters";
    public static final String USERNAME_SIZE = "Username must be between 3 and 50 characters";
    public static final String SERVICE_NAME_REQUIRED = "Service name is required";
    public static final String DESCRIPTION_REQUIRED = "Description is required";
    public static final String REFRESH_TOKEN_REQUIRED = "Refresh token is required";

    // Cache Names
    public static final String API_KEY_CACHE = "apiKeys";
    public static final String USER_CACHE = "users";

    // Token Types
    public static final String TOKEN_TYPE_BEARER = "Bearer";

    private ApiConstants() {
        // Private constructor to prevent instantiation
    }
}