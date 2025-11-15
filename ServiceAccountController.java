package com.bank.capp.controllers;

import com.bank.capp.constants.ApiConstants;
import com.bank.capp.models.*;
import com.bank.capp.services.ApiKeyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiConstants.SERVICE_ACCOUNT_BASE_PATH)
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Service Account Management", description = "Manage service accounts and API keys")
public class ServiceAccountController {

    private final ApiKeyService apiKeyService;

    @PostMapping("/generate-api-key")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Generate API key", description = "Generate a new API key for a service account (Admin only)")
    public ResponseEntity<ApiKeyResponse> generateApiKey(
            @Valid @RequestBody ApiKeyGenerationRequest request
    ) {
        String apiKey = apiKeyService.generateApiKey(
                request.getServiceName(),
                request.getDescription()
        );

        return ResponseEntity.ok(ApiKeyResponse.builder()
                .apiKey(apiKey)
                .serviceName(request.getServiceName())
                .message(ApiConstants.API_KEY_GENERATED)
                .build());
    }

    @PostMapping("/{id}/revoke")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Revoke API key", description = "Revoke an existing API key (Admin only)")
    public ResponseEntity<MessageResponse> revokeApiKey(@PathVariable Long id) {
        apiKeyService.revokeApiKey(id);
        return ResponseEntity.ok(new MessageResponse(ApiConstants.API_KEY_REVOKED));
    }

    @PostMapping("/{id}/rotate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Rotate API key", description = "Rotate API key (revoke old, generate new) (Admin only)")
    public ResponseEntity<ApiKeyResponse> rotateApiKey(@PathVariable Long id) {
        String newApiKey = apiKeyService.rotateApiKey(id);

        return ResponseEntity.ok(ApiKeyResponse.builder()
                .apiKey(newApiKey)
                .message(ApiConstants.API_KEY_ROTATED)
                .build());
    }

    @GetMapping("/{serviceName}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get service account", description = "Get service account details by name (Admin only)")
    public ResponseEntity<ServiceAccountResponse> getServiceAccount(
            @PathVariable String serviceName
    ) {
        ServiceAccount account = apiKeyService.getServiceAccount(serviceName)
                .orElseThrow(() -> new IllegalArgumentException(ApiConstants.SERVICE_ACCOUNT_NOT_FOUND));

        return ResponseEntity.ok(toServiceAccountResponse(account));
    }

    private ServiceAccountResponse toServiceAccountResponse(ServiceAccount account) {
        return ServiceAccountResponse.builder()
                .id(account.getId())
                .serviceName(account.getServiceName())
                .description(account.getDescription())
                .active(account.isActive())
                .createdAt(account.getCreatedAt())
                .lastUsedAt(account.getLastUsedAt())
                .revokedAt(account.getRevokedAt())
                .build();
    }
}