package com.unifiedapi.unifiedapimanager.controller;

import com.unifiedapi.unifiedapimanager.dto.WebhookCreateRequest;
import com.unifiedapi.unifiedapimanager.dto.WebhookResponse;
import com.unifiedapi.unifiedapimanager.model.User;
import com.unifiedapi.unifiedapimanager.service.WebhookService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/webhooks")
public class WebhookController {

    private final WebhookService webhookService;

    public WebhookController(WebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @PostMapping
    public ResponseEntity<WebhookResponse> createWebhook(@AuthenticationPrincipal User user,
                                                         @Valid @RequestBody WebhookCreateRequest request) {
        return ResponseEntity.ok(webhookService.createWebhook(user, request));
    }

    @GetMapping
    public ResponseEntity<List<WebhookResponse>> listWebhooks(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(webhookService.listWebhooks(user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWebhook(@AuthenticationPrincipal User user, @PathVariable UUID id) {
        webhookService.deleteWebhook(user, id);
        return ResponseEntity.noContent().build();
    }
}
