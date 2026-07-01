package com.unifiedapi.unifiedapimanager.service;

import com.unifiedapi.unifiedapimanager.model.ConnectedAccount;
import com.unifiedapi.unifiedapimanager.model.Platform;
import com.unifiedapi.unifiedapimanager.model.User;
import com.unifiedapi.unifiedapimanager.repository.ConnectedAccountRepository;
import com.unifiedapi.unifiedapimanager.repository.UserRepository;
import com.unifiedapi.unifiedapimanager.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Handles OAuth2 login/connection for LinkedIn and Twitter. This is the pragmatic MVP approach:
 * OAuth2 login creates a user if needed and stores/updates the connected account for that platform.
 */
@Service
public class OAuth2Service {

    private final UserRepository userRepository;
    private final ConnectedAccountRepository connectedAccountRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public OAuth2Service(UserRepository userRepository,
                         ConnectedAccountRepository connectedAccountRepository,
                         JwtTokenProvider jwtTokenProvider,
                         PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.connectedAccountRepository = connectedAccountRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public String processOAuth2Login(OAuth2AuthenticationToken token, OAuth2AuthorizedClient authorizedClient) {
        Platform platform = platformFromRegistration(token.getAuthorizedClientRegistrationId());
        OAuth2User oauthUser = token.getPrincipal();
        Map<String, Object> attributes = oauthUser.getAttributes();

        String email = extractEmail(platform, attributes);
        String providerUserId = extractProviderUserId(platform, attributes);
        String name = extractName(platform, attributes);

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> createUser(email, name));

        Optional<ConnectedAccount> existing = connectedAccountRepository.findByUserIdAndPlatform(user.getId(), platform);
        ConnectedAccount account = existing.orElse(ConnectedAccount.builder()
                .user(user)
                .platform(platform)
                .providerUserId(providerUserId)
                .status(ConnectedAccount.AccountStatus.ACTIVE)
                .build());
        account.setProviderUserId(providerUserId);
        account.setStatus(ConnectedAccount.AccountStatus.ACTIVE);
        if (authorizedClient != null) {
            account.setAccessToken(authorizedClient.getAccessToken().getTokenValue());
            if (authorizedClient.getRefreshToken() != null) {
                account.setRefreshToken(authorizedClient.getRefreshToken().getTokenValue());
            }
            if (authorizedClient.getAccessToken().getExpiresAt() != null) {
                account.setExpiresAt(authorizedClient.getAccessToken().getExpiresAt());
            }
        }
        connectedAccountRepository.save(account);

        return jwtTokenProvider.generateToken(user.getId(), user.getEmail());
    }

    private User createUser(String email, String name) {
        User user = User.builder()
                .email(email)
                .name(name != null ? name : email)
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .build();
        return userRepository.save(user);
    }

    private Platform platformFromRegistration(String registrationId) {
        return switch (registrationId.toLowerCase()) {
            case "linkedin" -> Platform.LINKEDIN;
            case "twitter" -> Platform.TWITTER;
            default -> Platform.MOCK;
        };
    }

    private String extractEmail(Platform platform, Map<String, Object> attributes) {
        if (attributes.containsKey("email")) {
            return (String) attributes.get("email");
        }
        // LinkedIn userinfo endpoint returns 'email' claim.
        // Twitter v2 userinfo returns 'data.username' and no email by default.
        return platform.name().toLowerCase() + "_" + extractProviderUserId(platform, attributes) + "@placeholder.local";
    }

    private String extractProviderUserId(Platform platform, Map<String, Object> attributes) {
        return switch (platform) {
            case LINKEDIN -> safeString(attributes.get("sub"));
            case TWITTER -> {
                Object data = attributes.get("data");
                if (data instanceof Map<?, ?> dataMap) {
                    yield safeString(dataMap.get("id"));
                }
                yield safeString(attributes.get("id"));
            }
            default -> UUID.randomUUID().toString();
        };
    }

    private String extractName(Platform platform, Map<String, Object> attributes) {
        if (attributes.containsKey("name")) {
            return (String) attributes.get("name");
        }
        return switch (platform) {
            case LINKEDIN -> safeString(attributes.get("name"));
            case TWITTER -> {
                Object data = attributes.get("data");
                if (data instanceof Map<?, ?> dataMap) {
                    yield safeString(dataMap.get("name"));
                }
                yield null;
            }
            default -> null;
        };
    }

    private String safeString(Object value) {
        return value != null ? value.toString() : null;
    }
}
