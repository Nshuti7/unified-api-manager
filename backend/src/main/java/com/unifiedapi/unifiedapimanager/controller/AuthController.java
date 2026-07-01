package com.unifiedapi.unifiedapimanager.controller;

import com.unifiedapi.unifiedapimanager.dto.AuthRequest;
import com.unifiedapi.unifiedapimanager.dto.AuthResponse;
import com.unifiedapi.unifiedapimanager.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "${frontend.url}")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/oauth/providers")
    public ResponseEntity<Map<String, String>> oauthProviders() {
        return ResponseEntity.ok(Map.of(
                "linkedin", "/oauth2/authorization/linkedin",
                "twitter", "/oauth2/authorization/twitter"
        ));
    }
}
