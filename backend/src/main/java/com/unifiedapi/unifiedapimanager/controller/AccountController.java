package com.unifiedapi.unifiedapimanager.controller;

import com.unifiedapi.unifiedapimanager.dto.ConnectedAccountResponse;
import com.unifiedapi.unifiedapimanager.model.Platform;
import com.unifiedapi.unifiedapimanager.model.User;
import com.unifiedapi.unifiedapimanager.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public ResponseEntity<List<ConnectedAccountResponse>> listAccounts(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(accountService.listAccounts(user));
    }

    @GetMapping("/available")
    public ResponseEntity<List<Platform>> availablePlatforms(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(accountService.availablePlatforms(user));
    }

    @GetMapping("/connect-urls")
    public ResponseEntity<Map<String, String>> connectUrls(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(Map.of(
                "LINKEDIN", "/oauth2/authorization/linkedin",
                "TWITTER", "/oauth2/authorization/twitter"
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> disconnectAccount(@AuthenticationPrincipal User user, @PathVariable UUID id) {
        accountService.disconnectAccount(user, id);
        return ResponseEntity.noContent().build();
    }
}
