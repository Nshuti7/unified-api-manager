package com.unifiedapi.unifiedapimanager.dto;

import com.unifiedapi.unifiedapimanager.model.Platform;
import com.unifiedapi.unifiedapimanager.model.Post;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class PublishResponse {
    private UUID postId;
    private String content;
    private Post.PostStatus status;
    private Instant scheduledAt;
    private Instant createdAt;
    private List<PlatformResult> results;

    @Data
    @Builder
    public static class PlatformResult {
        private Platform platform;
        private String status;
        private String externalPostId;
        private String errorMessage;
    }
}
