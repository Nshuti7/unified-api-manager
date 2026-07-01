package com.unifiedapi.unifiedapimanager.repository;

import com.unifiedapi.unifiedapimanager.model.Webhook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WebhookRepository extends JpaRepository<Webhook, UUID> {
    List<Webhook> findByUserId(UUID userId);
    List<Webhook> findByUserIdAndActiveTrue(UUID userId);
}
