package com.unifiedapi.unifiedapimanager.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class DashboardStats {
    private long totalPosts;
    private long scheduledPosts;
    private long publishedPosts;
    private long failedPosts;
    private long connectedAccounts;
    private long activeApiKeys;
    private long activeWebhooks;
    private Map<String, Long> postsByPlatform;
}
