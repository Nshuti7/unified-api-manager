package com.unifiedapi.unifiedapimanager.platform;

import com.unifiedapi.unifiedapimanager.model.ConnectedAccount;
import com.unifiedapi.unifiedapimanager.model.Platform;
import com.unifiedapi.unifiedapimanager.model.Post;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
public class TwitterAdapter implements PlatformAdapter {

    private final WebClient webClient;

    public TwitterAdapter(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://api.twitter.com").build();
    }

    @Override
    public Platform getPlatform() {
        return Platform.TWITTER;
    }

    @Override
    public void validate(Post post) {
        if (post.getContent() == null || post.getContent().isBlank()) {
            throw new IllegalArgumentException("Twitter posts must have text");
        }
        // Twitter's basic text limit is 280 characters for most accounts.
        if (post.getContent().length() > 280) {
            throw new IllegalArgumentException("Twitter posts must be under 280 characters");
        }
    }

    @Override
    public PublishResult publish(Post post, ConnectedAccount account) {
        try {
            Map<String, Object> body = Map.of("text", post.getContent());

            Map<String, Object> response = webClient.post()
                    .uri("/2/tweets")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + account.getAccessToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            Object data = response != null ? response.get("data") : null;
            String externalId = null;
            if (data instanceof Map<?, ?> dataMap) {
                externalId = (String) dataMap.get("id");
            }
            return PublishResult.success(externalId != null ? externalId : "twitter_post");
        } catch (Exception e) {
            return PublishResult.failure("Twitter publish failed: " + e.getMessage());
        }
    }
}
