package com.unifiedapi.unifiedapimanager.security;

import com.unifiedapi.unifiedapimanager.model.ApiKey;
import com.unifiedapi.unifiedapimanager.model.User;
import com.unifiedapi.unifiedapimanager.repository.ApiKeyRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private final ApiKeyRepository apiKeyRepository;
    private final ApiKeyUtil apiKeyUtil;

    public ApiKeyAuthenticationFilter(ApiKeyRepository apiKeyRepository, ApiKeyUtil apiKeyUtil) {
        this.apiKeyRepository = apiKeyRepository;
        this.apiKeyUtil = apiKeyUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String apiKey = request.getHeader("X-API-Key");
            if (StringUtils.hasText(apiKey)) {
                String hash = apiKeyUtil.hashApiKey(apiKey);
                Optional<ApiKey> keyOpt = apiKeyRepository.findByKeyHash(hash);
                if (keyOpt.isPresent() && !keyOpt.get().isRevoked()) {
                    ApiKey key = keyOpt.get();
                    key.setLastUsedAt(java.time.Instant.now());
                    apiKeyRepository.save(key);
                    User user = key.getUser();
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            user, null, Collections.emptyList());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        } catch (Exception e) {
            logger.error("API key authentication failed", e);
        }
        filterChain.doFilter(request, response);
    }
}
