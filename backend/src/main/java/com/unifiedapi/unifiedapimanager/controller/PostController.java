package com.unifiedapi.unifiedapimanager.controller;

import com.unifiedapi.unifiedapimanager.dto.CreatePostRequest;
import com.unifiedapi.unifiedapimanager.dto.PublishResponse;
import com.unifiedapi.unifiedapimanager.model.User;
import com.unifiedapi.unifiedapimanager.service.PostService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public ResponseEntity<PublishResponse> createPost(@AuthenticationPrincipal User user,
                                                      @Valid @RequestBody CreatePostRequest request) {
        return ResponseEntity.ok(postService.createPost(user, request));
    }

    @GetMapping
    public ResponseEntity<List<PublishResponse>> listPosts(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(postService.listPosts(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PublishResponse> getPost(@AuthenticationPrincipal User user, @PathVariable UUID id) {
        return ResponseEntity.ok(postService.getPost(user, id));
    }
}
