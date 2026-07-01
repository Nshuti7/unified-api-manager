package com.unifiedapi.unifiedapimanager.service;

import com.unifiedapi.unifiedapimanager.dto.CreatePostRequest;
import com.unifiedapi.unifiedapimanager.dto.PublishResponse;
import com.unifiedapi.unifiedapimanager.model.ConnectedAccount;
import com.unifiedapi.unifiedapimanager.model.Platform;
import com.unifiedapi.unifiedapimanager.model.Post;
import com.unifiedapi.unifiedapimanager.model.PostPlatform;
import com.unifiedapi.unifiedapimanager.model.User;
import com.unifiedapi.unifiedapimanager.platform.PlatformAdapter;
import com.unifiedapi.unifiedapimanager.platform.PlatformAdapterRegistry;
import com.unifiedapi.unifiedapimanager.platform.PublishResult;
import com.unifiedapi.unifiedapimanager.repository.ConnectedAccountRepository;
import com.unifiedapi.unifiedapimanager.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final ConnectedAccountRepository connectedAccountRepository;
    private final PlatformAdapterRegistry adapterRegistry;
    private final WebhookService webhookService;

    public PostService(PostRepository postRepository,
                       ConnectedAccountRepository connectedAccountRepository,
                       PlatformAdapterRegistry adapterRegistry,
                       WebhookService webhookService) {
        this.postRepository = postRepository;
        this.connectedAccountRepository = connectedAccountRepository;
        this.adapterRegistry = adapterRegistry;
        this.webhookService = webhookService;
    }

    @Transactional
    public PublishResponse createPost(User user, CreatePostRequest request) {
        Post.PostStatus status = request.getScheduledAt() != null && request.getScheduledAt().isAfter(Instant.now())
                ? Post.PostStatus.SCHEDULED
                : Post.PostStatus.DRAFT;

        Post post = Post.builder()
                .user(user)
                .content(request.getContent())
                .mediaUrls(request.getMediaUrls() != null ? request.getMediaUrls() : List.of())
                .status(status)
                .scheduledAt(request.getScheduledAt())
                .build();

        List<PostPlatform> platforms = new ArrayList<>();
        for (Platform platform : request.getPlatforms()) {
            PlatformAdapter adapter = adapterRegistry.getAdapter(platform)
                    .orElseThrow(() -> new IllegalArgumentException("Platform not supported: " + platform));
            adapter.validate(post);
            platforms.add(PostPlatform.builder()
                    .post(post)
                    .platform(platform)
                    .status(PostPlatform.DeliveryStatus.PENDING)
                    .build());
        }
        post.setPlatforms(platforms);
        Post saved = postRepository.save(post);

        webhookService.sendEvent(user.getId(), "post.created", saved.getId().toString());

        if (status == Post.PostStatus.DRAFT) {
            return publishPost(saved);
        }
        return toResponse(saved);
    }

    @Transactional
    public PublishResponse publishPost(Post post) {
        post.setStatus(Post.PostStatus.PUBLISHING);
        postRepository.save(post);

        int successes = 0;
        int failures = 0;
        for (PostPlatform pp : post.getPlatforms()) {
            PlatformAdapter adapter = adapterRegistry.getAdapter(pp.getPlatform()).orElse(null);
            if (adapter == null) {
                pp.setStatus(PostPlatform.DeliveryStatus.FAILED);
                pp.setErrorMessage("No adapter available");
                failures++;
                continue;
            }

            ConnectedAccount account = connectedAccountRepository
                    .findByUserIdAndPlatform(post.getUser().getId(), pp.getPlatform())
                    .orElse(null);
            if (account == null || account.getStatus() != ConnectedAccount.AccountStatus.ACTIVE) {
                pp.setStatus(PostPlatform.DeliveryStatus.FAILED);
                pp.setErrorMessage("No active connected account for " + pp.getPlatform());
                failures++;
                continue;
            }

            pp.setStatus(PostPlatform.DeliveryStatus.PROCESSING);
            postRepository.save(post);

            PublishResult result = publishWithRetry(adapter, post, account, 3);
            if (result.isSuccess()) {
                pp.setStatus(PostPlatform.DeliveryStatus.SUCCESS);
                pp.setExternalPostId(result.getExternalPostId());
                successes++;
            } else {
                pp.setStatus(PostPlatform.DeliveryStatus.FAILED);
                pp.setErrorMessage(result.getErrorMessage());
                failures++;
            }
        }

        if (failures == 0 && successes > 0) {
            post.setStatus(Post.PostStatus.PUBLISHED);
            post.setPublishedAt(Instant.now());
            webhookService.sendEvent(post.getUser().getId(), "post.published", post.getId().toString());
        } else if (successes > 0) {
            post.setStatus(Post.PostStatus.PARTIAL_FAILURE);
            webhookService.sendEvent(post.getUser().getId(), "post.partial_failure", post.getId().toString());
        } else {
            post.setStatus(Post.PostStatus.FAILED);
            webhookService.sendEvent(post.getUser().getId(), "post.failed", post.getId().toString());
        }
        postRepository.save(post);
        return toResponse(post);
    }

    private PublishResult publishWithRetry(PlatformAdapter adapter, Post post, ConnectedAccount account, int maxAttempts) {
        int attempt = 0;
        while (true) {
            attempt++;
            PublishResult result = adapter.publish(post, account);
            if (result.isSuccess() || attempt >= maxAttempts) {
                return result;
            }
            try {
                Thread.sleep((long) Math.pow(2, attempt) * 500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return result;
            }
        }
    }

    @Transactional
    public void publishDueScheduledPosts() {
        List<Post> due = postRepository.findDueScheduledPosts(Instant.now());
        for (Post post : due) {
            try {
                publishPost(post);
            } catch (Exception e) {
                // Scheduled job should not fail the whole batch. Log and continue.
                e.printStackTrace();
            }
        }
    }

    public List<PublishResponse> listPosts(User user) {
        return postRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public PublishResponse getPost(User user, UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        if (!post.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Not authorized");
        }
        return toResponse(post);
    }

    private PublishResponse toResponse(Post post) {
        List<PublishResponse.PlatformResult> results = post.getPlatforms().stream()
                .map(pp -> PublishResponse.PlatformResult.builder()
                        .platform(pp.getPlatform())
                        .status(pp.getStatus().name())
                        .externalPostId(pp.getExternalPostId())
                        .errorMessage(pp.getErrorMessage())
                        .build())
                .collect(Collectors.toList());

        return PublishResponse.builder()
                .postId(post.getId())
                .content(post.getContent())
                .status(post.getStatus())
                .scheduledAt(post.getScheduledAt())
                .createdAt(post.getCreatedAt())
                .results(results)
                .build();
    }
}
