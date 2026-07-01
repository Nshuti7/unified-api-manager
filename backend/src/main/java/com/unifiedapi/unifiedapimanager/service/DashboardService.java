package com.unifiedapi.unifiedapimanager.service;

import com.unifiedapi.unifiedapimanager.dto.DashboardStats;
import com.unifiedapi.unifiedapimanager.model.Post;
import com.unifiedapi.unifiedapimanager.model.User;
import com.unifiedapi.unifiedapimanager.repository.ApiKeyRepository;
import com.unifiedapi.unifiedapimanager.repository.ConnectedAccountRepository;
import com.unifiedapi.unifiedapimanager.repository.PostRepository;
import com.unifiedapi.unifiedapimanager.repository.WebhookRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final PostRepository postRepository;
    private final ConnectedAccountRepository connectedAccountRepository;
    private final ApiKeyRepository apiKeyRepository;
    private final WebhookRepository webhookRepository;

    public DashboardService(PostRepository postRepository,
                            ConnectedAccountRepository connectedAccountRepository,
                            ApiKeyRepository apiKeyRepository,
                            WebhookRepository webhookRepository) {
        this.postRepository = postRepository;
        this.connectedAccountRepository = connectedAccountRepository;
        this.apiKeyRepository = apiKeyRepository;
        this.webhookRepository = webhookRepository;
    }

    public DashboardStats getStats(User user) {
        List<Post> posts = postRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        long scheduled = posts.stream().filter(p -> p.getStatus() == Post.PostStatus.SCHEDULED).count();
        long published = posts.stream().filter(p -> p.getStatus() == Post.PostStatus.PUBLISHED).count();
        long failed = posts.stream().filter(p -> p.getStatus() == Post.PostStatus.FAILED || p.getStatus() == Post.PostStatus.PARTIAL_FAILURE).count();

        Map<String, Long> byPlatform = new HashMap<>();
        for (Post post : posts) {
            post.getPlatforms().forEach(pp -> {
                String key = pp.getPlatform().name();
                byPlatform.merge(key, 1L, Long::sum);
            });
        }

        return DashboardStats.builder()
                .totalPosts(posts.size())
                .scheduledPosts(scheduled)
                .publishedPosts(published)
                .failedPosts(failed)
                .connectedAccounts(connectedAccountRepository.findByUserId(user.getId()).size())
                .activeApiKeys(apiKeyRepository.findByUserIdAndRevokedFalse(user.getId()).size())
                .activeWebhooks(webhookRepository.findByUserIdAndActiveTrue(user.getId()).size())
                .postsByPlatform(byPlatform)
                .build();
    }
}
