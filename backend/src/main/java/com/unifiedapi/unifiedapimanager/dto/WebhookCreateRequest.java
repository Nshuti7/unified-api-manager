package com.unifiedapi.unifiedapimanager.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WebhookCreateRequest {
    @NotBlank
    private String url;
    @NotBlank
    private String events; // comma-separated, e.g. "post.published,post.failed"
    private String secret;
}
