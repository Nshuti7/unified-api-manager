package com.unifiedapi.unifiedapimanager.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class WebhookResponse {
    private UUID id;
    private String url;
    private String events;
    private String secret;
    private boolean active;
    private Instant createdAt;
}
