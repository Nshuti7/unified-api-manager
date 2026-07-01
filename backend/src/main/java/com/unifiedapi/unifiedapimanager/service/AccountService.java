package com.unifiedapi.unifiedapimanager.service;

import com.unifiedapi.unifiedapimanager.dto.ConnectedAccountResponse;
import com.unifiedapi.unifiedapimanager.model.ConnectedAccount;
import com.unifiedapi.unifiedapimanager.model.Platform;
import com.unifiedapi.unifiedapimanager.model.User;
import com.unifiedapi.unifiedapimanager.repository.ConnectedAccountRepository;
import com.unifiedapi.unifiedapimanager.platform.PlatformAdapterRegistry;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AccountService {

    private final ConnectedAccountRepository connectedAccountRepository;
    private final PlatformAdapterRegistry registry;

    public AccountService(ConnectedAccountRepository connectedAccountRepository,
                          PlatformAdapterRegistry registry) {
        this.connectedAccountRepository = connectedAccountRepository;
        this.registry = registry;
    }

    public List<ConnectedAccountResponse> listAccounts(User user) {
        return connectedAccountRepository.findByUserId(user.getId()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<Platform> availablePlatforms(User user) {
        List<Platform> connected = connectedAccountRepository.findByUserId(user.getId()).stream()
                .map(ConnectedAccount::getPlatform)
                .toList();
        return registry.supportedPlatforms().stream()
                .filter(p -> !connected.contains(p))
                .collect(Collectors.toList());
    }

    public void disconnectAccount(User user, UUID accountId) {
        ConnectedAccount account = connectedAccountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        if (!account.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Not authorized");
        }
        account.setStatus(ConnectedAccount.AccountStatus.REVOKED);
        connectedAccountRepository.save(account);
    }

    private ConnectedAccountResponse toResponse(ConnectedAccount account) {
        return ConnectedAccountResponse.builder()
                .id(account.getId())
                .platform(account.getPlatform())
                .providerUserId(account.getProviderUserId())
                .status(account.getStatus())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}
