package com.unifiedapi.unifiedapimanager.dto;

import com.unifiedapi.unifiedapimanager.model.ConnectedAccount;
import com.unifiedapi.unifiedapimanager.model.Platform;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class ConnectedAccountResponse {
    private UUID id;
    private Platform platform;
    private String providerUserId;
    private ConnectedAccount.AccountStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}
