package com.unifiedapi.unifiedapimanager.platform;

import com.unifiedapi.unifiedapimanager.model.ConnectedAccount;
import com.unifiedapi.unifiedapimanager.model.Platform;
import com.unifiedapi.unifiedapimanager.model.Post;

/**
 * Adapter interface for all social platforms. Every new platform is a new implementation
 * registered in {@link PlatformAdapterRegistry}. The core API never touches platform-specific APIs directly.
 */
public interface PlatformAdapter {
    Platform getPlatform();

    default boolean supports(Platform platform) {
        return getPlatform() == platform;
    }

    /**
     * Validate that the post can be published on this platform before attempting the network call.
     * Throw IllegalArgumentException with a clear message if invalid.
     */
    void validate(Post post);

    /**
     * Publish the post to the platform. Returns the external post id or null.
     */
    PublishResult publish(Post post, ConnectedAccount account);

    /**
     * Refresh an access token if expired. Returns the refreshed account or null if unsupported.
     */
    default ConnectedAccount refreshToken(ConnectedAccount account) {
        return null;
    }
}
