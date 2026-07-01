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
public class LinkedInAdapter implements PlatformAdapter {

    private final WebClient webClient;

    public LinkedInAdapter(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://api.linkedin.com").build();
    }

    @Override
    public Platform getPlatform() {
        return Platform.LINKEDIN;
    }

    @Override
    public void validate(Post post) {
        if (post.getContent() == null || post.getContent().isBlank()) {
            throw new IllegalArgumentException("LinkedIn posts must have content");
        }
        if (post.getContent().length() > 3000) {
            throw new IllegalArgumentException("LinkedIn posts must be under 3000 characters");
        }
    }

    @Override
    public PublishResult publish(Post post, ConnectedAccount account) {
        try {
            String author = "urn:li:person:" + account.getProviderUserId();
            Map<String, Object> body = Map.of(
                    "author", author,
                    "commentary", post.getContent(),
                    "visibility", "PUBLIC",
                    "distribution", Map.of(
                            "feedDistribution", "MAIN_FEED",
                            "targetEntities", java.util.List.of(),
                            "thirdPartyDistributionChannels", java.util.List.of()
                    )
            );

            var response = webClient.post()
                    .uri("/rest/posts")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + account.getAccessToken())
                    .header("LinkedIn-Version", "202401")
                    .header("X-Restli-Protocol-Version", "2.0.0")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            // LinkedIn returns the new entity URN in the x-restli-id header.
            String externalId = response.getHeaders().getFirst("x-restli-id");
            return PublishResult.success(externalId != null ? externalId : "linkedin_post");
        } catch (Exception e) {
            return PublishResult.failure("LinkedIn publish failed: " + e.getMessage());
        }
    }
}
