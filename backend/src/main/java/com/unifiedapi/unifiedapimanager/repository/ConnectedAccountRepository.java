package com.unifiedapi.unifiedapimanager.repository;

import com.unifiedapi.unifiedapimanager.model.ConnectedAccount;
import com.unifiedapi.unifiedapimanager.model.Platform;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConnectedAccountRepository extends JpaRepository<ConnectedAccount, UUID> {
    List<ConnectedAccount> findByUserId(UUID userId);
    Optional<ConnectedAccount> findByUserIdAndPlatform(UUID userId, Platform platform);
    boolean existsByUserIdAndPlatform(UUID userId, Platform platform);
}
