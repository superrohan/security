package com.bank.capp.controllers;

import com.bank.capp.constants.ApiConstants;
import com.bank.capp.models.*;
import com.bank.capp.services.ServiceAccountAuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiConstants.API_BASE_PATH + "/service-auth")
@RequiredArgsConstructor
@Tag(name = "Service Account Authentication", description = "Self-service authentication for service accounts")
public class ServiceAccountAuthenticationController {

    private final ServiceAccountAuthenticationService serviceAuthService;

    @PostMapping("/login")
    @Operation(summary = "Service account login", description = "Authenticate service account with API key and get JWT tokens")
    public ResponseEntity<ServiceAccountAuthenticationResponse> login(
            @Valid @RequestBody ServiceAccountLoginRequest request
    ) {
        return ResponseEntity.ok(serviceAuthService.authenticate(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh service access token", description = "Get a new access token using refresh token")
    public ResponseEntity<ServiceAccountAuthenticationResponse> refreshToken(
            @Valid @RequestBody ServiceAccountRefreshRequest request
    ) {
        return ResponseEntity.ok(serviceAuthService.refreshToken(request));
    }

    @PostMapping("/logout")
    @Operation(summary = "Service account logout", description = "Revoke refresh token and logout service")
    public ResponseEntity<MessageResponse> logout(
            @Valid @RequestBody ServiceAccountRefreshRequest request
    ) {
        serviceAuthService.logout(request);
        return ResponseEntity.ok(new MessageResponse(ApiConstants.LOGOUT_SUCCESS));
    }
}
