package com.unifiedapi.unifiedapimanager.platform;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublishResult {
    private boolean success;
    private String externalPostId;
    private String errorMessage;

    public static PublishResult success(String externalPostId) {
        return PublishResult.builder().success(true).externalPostId(externalPostId).build();
    }

    public static PublishResult failure(String errorMessage) {
        return PublishResult.builder().success(false).errorMessage(errorMessage).build();
    }
}
