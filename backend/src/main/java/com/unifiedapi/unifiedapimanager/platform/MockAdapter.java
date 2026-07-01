package com.unifiedapi.unifiedapimanager.platform;

import com.unifiedapi.unifiedapimanager.model.ConnectedAccount;
import com.unifiedapi.unifiedapimanager.model.Platform;
import com.unifiedapi.unifiedapimanager.model.Post;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Simulates a platform. Useful for integration tests and demos without real OAuth credentials.
 */
@Component
public class MockAdapter implements PlatformAdapter {

    @Override
    public Platform getPlatform() {
        return Platform.MOCK;
    }

    @Override
    public void validate(Post post) {
        if (post.getContent() == null || post.getContent().isBlank()) {
            throw new IllegalArgumentException("MOCK posts must have content");
        }
    }

    @Override
    public PublishResult publish(Post post, ConnectedAccount account) {
        // Simulate a tiny bit of network delay and a deterministic fake external id.
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return PublishResult.success("mock_" + UUID.randomUUID().toString().substring(0, 8));
    }
}
