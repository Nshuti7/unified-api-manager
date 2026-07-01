package com.unifiedapi.unifiedapimanager.scheduler;

import com.unifiedapi.unifiedapimanager.service.PostService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PostScheduler {

    private final PostService postService;

    public PostScheduler(PostService postService) {
        this.postService = postService;
    }

    @Scheduled(fixedDelayString = "${scheduler.publish.delay:60000}")
    public void publishScheduledPosts() {
        postService.publishDueScheduledPosts();
    }
}
