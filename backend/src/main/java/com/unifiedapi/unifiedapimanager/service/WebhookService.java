package com.unifiedapi.unifiedapimanager.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unifiedapi.unifiedapimanager.dto.WebhookCreateRequest;
import com.unifiedapi.unifiedapimanager.dto.WebhookResponse;
import com.unifiedapi.unifiedapimanager.model.User;
import com.unifiedapi.unifiedapimanager.model.Webhook;
import com.unifiedapi.unifiedapimanager.model.WebhookDelivery;
import com.unifiedapi.unifiedapimanager.repository.WebhookDeliveryRepository;
import com.unifiedapi.unifiedapimanager.repository.WebhookRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class WebhookService {

    private final WebhookRepository webhookRepository;
    private final WebhookDeliveryRepository webhookDeliveryRepository;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public WebhookService(WebhookRepository webhookRepository,
                          WebhookDeliveryRepository webhookDeliveryRepository,
                          WebClient.Builder webClientBuilder,
                          ObjectMapper objectMapper) {
        this.webhookRepository = webhookRepository;
        this.webhookDeliveryRepository = webhookDeliveryRepository;
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    public WebhookResponse createWebhook(User user, WebhookCreateRequest request) {
        Webhook webhook = Webhook.builder()
                .user(user)
                .url(request.getUrl())
                .events(request.getEvents())
                .secret(request.getSecret())
                .active(true)
                .build();
        webhookRepository.save(webhook);
        return toResponse(webhook);
    }

    public List<WebhookResponse> listWebhooks(User user) {
        return webhookRepository.findByUserId(user.getId()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public void deleteWebhook(User user, UUID webhookId) {
        Webhook webhook = webhookRepository.findById(webhookId)
                .orElseThrow(() -> new IllegalArgumentException("Webhook not found"));
        if (!webhook.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Not authorized");
        }
        webhookRepository.delete(webhook);
    }

    @Async("webhookExecutor")
    public void sendEvent(UUID userId, String event, String payload) {
        List<Webhook> webhooks = webhookRepository.findByUserIdAndActiveTrue(userId);
        for (Webhook webhook : webhooks) {
            if (!webhook.listensFor(event)) continue;
            deliver(webhook, event, payload);
        }
    }

    private void deliver(Webhook webhook, String event, String payload) {
        WebhookDelivery delivery = WebhookDelivery.builder()
                .webhook(webhook)
                .event(event)
                .payload(payload)
                .status(WebhookDelivery.DeliveryStatus.PENDING)
                .build();
        webhookDeliveryRepository.save(delivery);

        try {
            String body = objectMapper.writeValueAsString(Map.of(
                    "event", event,
                    "payload", payload,
                    "timestamp", Instant.now().toString()
            ));

            var response = webClient.post()
                    .uri(webhook.getUrl())
                    .header("X-Webhook-Event", event)
                    .header("X-Webhook-Signature", computeSignature(webhook.getSecret(), body))
                    .bodyValue(body)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            delivery.setStatus(WebhookDelivery.DeliveryStatus.DELIVERED);
            delivery.setResponseStatus(response.getStatusCode().value());
        } catch (JsonProcessingException e) {
            delivery.setStatus(WebhookDelivery.DeliveryStatus.FAILED);
            delivery.setResponseBody(e.getMessage());
        } catch (Exception e) {
            delivery.setStatus(WebhookDelivery.DeliveryStatus.FAILED);
            delivery.setResponseBody(e.getMessage());
        }
        webhookDeliveryRepository.save(delivery);
    }

    private String computeSignature(String secret, String body) {
        if (secret == null || secret.isBlank()) return "";
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            mac.init(new javax.crypto.spec.SecretKeySpec(secret.getBytes(), "HmacSHA256"));
            return java.util.Base64.getEncoder().encodeToString(mac.doFinal(body.getBytes()));
        } catch (Exception e) {
            return "";
        }
    }

    private WebhookResponse toResponse(Webhook webhook) {
        return WebhookResponse.builder()
                .id(webhook.getId())
                .url(webhook.getUrl())
                .events(webhook.getEvents())
                .secret(webhook.getSecret())
                .active(webhook.isActive())
                .createdAt(webhook.getCreatedAt())
                .build();
    }
}
