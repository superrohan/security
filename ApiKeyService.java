package com.bank.capp.services;

import com.bank.capp.constants.ApiConstants;
import com.bank.capp.models.ServiceAccount;
import com.bank.capp.repository.ServiceAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiKeyService {

    private final ServiceAccountRepository serviceAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final int API_KEY_LENGTH = 32;

    /**
     * Validate API key and return associated service account
     * Uses caching to reduce database hits for frequently used keys
     */
    @Cacheable(value = ApiConstants.API_KEY_CACHE, key = "#apiKey")
    public ServiceAccount validateApiKey(String apiKey) {
        Optional<ServiceAccount> serviceAccountOpt =
                serviceAccountRepository.findByApiKeyHash(hashApiKey(apiKey));

        if (serviceAccountOpt.isPresent()) {
            ServiceAccount account = serviceAccountOpt.get();

            // Update last used timestamp
            account.setLastUsedAt(LocalDateTime.now());
            serviceAccountRepository.save(account);

            return account;
        }

        return null;
    }

    /**
     * Generate a new API key for a service
     */
    public String generateApiKey(String serviceName, String description) {
        // Generate cryptographically secure random API key
        byte[] randomBytes = new byte[API_KEY_LENGTH];
        secureRandom.nextBytes(randomBytes);
        String apiKey = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        // Create service account
        ServiceAccount serviceAccount = ServiceAccount.builder()
                .serviceName(serviceName)
                .description(description)
                .apiKeyHash(hashApiKey(apiKey))
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        serviceAccountRepository.save(serviceAccount);

        log.info("Generated new API key for service: {}", serviceName);

        // Return the plain API key (only time it's visible)
        return apiKey;
    }

    /**
     * Revoke an API key
     */
    public void revokeApiKey(Long serviceAccountId) {
        serviceAccountRepository.findById(serviceAccountId).ifPresent(account -> {
            account.setActive(false);
            account.setRevokedAt(LocalDateTime.now());
            serviceAccountRepository.save(account);
            log.info("Revoked API key for service: {}", account.getServiceName());
        });
    }

    /**
     * Rotate API key (revoke old, generate new)
     */
    public String rotateApiKey(Long serviceAccountId) {
        Optional<ServiceAccount> accountOpt = serviceAccountRepository.findById(serviceAccountId);

        if (accountOpt.isPresent()) {
            ServiceAccount account = accountOpt.get();

            // Revoke old key
            account.setActive(false);
            account.setRevokedAt(LocalDateTime.now());
            serviceAccountRepository.save(account);

            // Generate new key
            return generateApiKey(account.getServiceName(), account.getDescription());
        }

        throw new IllegalArgumentException(ApiConstants.SERVICE_ACCOUNT_NOT_FOUND);
    }

    /**
     * Hash API key before storing (never store plain text)
     */
    private String hashApiKey(String apiKey) {
        return passwordEncoder.encode(apiKey);
    }

    /**
     * Get service account by name
     */
    public Optional<ServiceAccount> getServiceAccount(String serviceName) {
        return serviceAccountRepository.findByServiceName(serviceName);
    }
}