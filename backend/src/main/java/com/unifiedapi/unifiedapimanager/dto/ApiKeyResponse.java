package com.unifiedapi.unifiedapimanager.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class ApiKeyResponse {
    private UUID id;
    private String name;
    private String keyPrefix;
    private String fullKey; // only returned once on creation
    private Instant createdAt;
    private Instant lastUsedAt;
    private boolean revoked;
}
