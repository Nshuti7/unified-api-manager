package com.unifiedapi.unifiedapimanager.service;

import com.unifiedapi.unifiedapimanager.dto.ApiKeyCreateRequest;
import com.unifiedapi.unifiedapimanager.dto.ApiKeyResponse;
import com.unifiedapi.unifiedapimanager.model.ApiKey;
import com.unifiedapi.unifiedapimanager.model.User;
import com.unifiedapi.unifiedapimanager.repository.ApiKeyRepository;
import com.unifiedapi.unifiedapimanager.security.ApiKeyUtil;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final ApiKeyUtil apiKeyUtil;

    public ApiKeyService(ApiKeyRepository apiKeyRepository, ApiKeyUtil apiKeyUtil) {
        this.apiKeyRepository = apiKeyRepository;
        this.apiKeyUtil = apiKeyUtil;
    }

    public ApiKeyResponse createApiKey(User user, ApiKeyCreateRequest request) {
        String fullKey = apiKeyUtil.generateApiKey();
        String hash = apiKeyUtil.hashApiKey(fullKey);
        ApiKey apiKey = ApiKey.builder()
                .name(request.getName())
                .keyHash(hash)
                .keyPrefix(fullKey.substring(0, Math.min(12, fullKey.length())))
                .user(user)
                .revoked(false)
                .build();
        apiKeyRepository.save(apiKey);
        return ApiKeyResponse.builder()
                .id(apiKey.getId())
                .name(apiKey.getName())
                .keyPrefix(apiKey.getKeyPrefix())
                .fullKey(fullKey) // shown only once
                .createdAt(apiKey.getCreatedAt())
                .lastUsedAt(apiKey.getLastUsedAt())
                .revoked(apiKey.isRevoked())
                .build();
    }

    public List<ApiKeyResponse> listApiKeys(User user) {
        return apiKeyRepository.findByUserIdAndRevokedFalse(user.getId()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public void revokeApiKey(User user, UUID keyId) {
        ApiKey key = apiKeyRepository.findById(keyId)
                .orElseThrow(() -> new IllegalArgumentException("API key not found"));
        if (!key.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Not authorized to revoke this key");
        }
        key.setRevoked(true);
        apiKeyRepository.save(key);
    }

    private ApiKeyResponse toResponse(ApiKey apiKey) {
        return ApiKeyResponse.builder()
                .id(apiKey.getId())
                .name(apiKey.getName())
                .keyPrefix(apiKey.getKeyPrefix())
                .createdAt(apiKey.getCreatedAt())
                .lastUsedAt(apiKey.getLastUsedAt())
                .revoked(apiKey.isRevoked())
                .build();
    }
}
