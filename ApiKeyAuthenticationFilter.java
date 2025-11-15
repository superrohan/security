package com.bank.capp.security;

import com.bank.capp.constants.SecurityConstants;
import com.bank.capp.services.ApiKeyService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private final ApiKeyService apiKeyService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Skip if this is a public endpoint
        if (isPublicEndpoint(request.getServletPath())) {
            filterChain.doFilter(request, response);
            return;
        }

        final String apiKey = request.getHeader(SecurityConstants.API_KEY_HEADER);

        if (apiKey != null && !apiKey.isEmpty()) {
            try {
                // Validate API Key and get service details
                var serviceAccount = apiKeyService.validateApiKey(apiKey);

                if (serviceAccount != null && serviceAccount.isActive()) {
                    // Create authentication token for service account
                    var authorities = Collections.singletonList(
                            new SimpleGrantedAuthority(SecurityConstants.ROLE_SERVICE)
                    );

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    serviceAccount.getServiceName(),
                                    null,
                                    authorities
                            );

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.info("Service '{}' authenticated via API Key",
                            serviceAccount.getServiceName());

                    // Add service name to request attribute for auditing
                    request.setAttribute("authenticatedService",
                            serviceAccount.getServiceName());
                } else {
                    log.warn("Invalid or inactive API key provided");
                }
            } catch (Exception e) {
                log.error("API Key authentication failed", e);
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicEndpoint(String path) {
        for (String endpoint : SecurityConstants.PUBLIC_ENDPOINTS) {
            if (endpoint.endsWith("/**")) {
                String baseEndpoint = endpoint.substring(0, endpoint.length() - 3);
                if (path.startsWith(baseEndpoint)) {
                    return true;
                }
            } else if (path.equals(endpoint)) {
                return true;
            }
        }
        return false;
    }
}