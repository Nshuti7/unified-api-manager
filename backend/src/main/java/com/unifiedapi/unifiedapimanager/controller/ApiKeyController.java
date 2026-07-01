package com.unifiedapi.unifiedapimanager.controller;

import com.unifiedapi.unifiedapimanager.dto.ApiKeyCreateRequest;
import com.unifiedapi.unifiedapimanager.dto.ApiKeyResponse;
import com.unifiedapi.unifiedapimanager.model.User;
import com.unifiedapi.unifiedapimanager.service.ApiKeyService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/keys")
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    public ApiKeyController(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @PostMapping
    public ResponseEntity<ApiKeyResponse> createKey(@AuthenticationPrincipal User user,
                                                      @Valid @RequestBody ApiKeyCreateRequest request) {
        return ResponseEntity.ok(apiKeyService.createApiKey(user, request));
    }

    @GetMapping
    public ResponseEntity<List<ApiKeyResponse>> listKeys(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(apiKeyService.listApiKeys(user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> revokeKey(@AuthenticationPrincipal User user, @PathVariable UUID id) {
        apiKeyService.revokeApiKey(user, id);
        return ResponseEntity.noContent().build();
    }
}
