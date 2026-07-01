package com.unifiedapi.unifiedapimanager.controller;

import com.unifiedapi.unifiedapimanager.dto.CreatePostRequest;
import com.unifiedapi.unifiedapimanager.dto.PublishResponse;
import com.unifiedapi.unifiedapimanager.model.User;
import com.unifiedapi.unifiedapimanager.service.PostService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Public API endpoint intended for server-to-server integrations. Authenticate with X-API-Key header.
 */
@RestController
@RequestMapping("/api/v1/publish")
public class PublishController {

    private final PostService postService;

    public PublishController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public ResponseEntity<PublishResponse> publish(@AuthenticationPrincipal User user,
                                                   @Valid @RequestBody CreatePostRequest request) {
        return ResponseEntity.ok(postService.createPost(user, request));
    }
}
