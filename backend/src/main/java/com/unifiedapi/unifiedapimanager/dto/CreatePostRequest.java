package com.unifiedapi.unifiedapimanager.dto;

import com.unifiedapi.unifiedapimanager.model.Platform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class CreatePostRequest {
    @NotBlank
    private String content;
    private List<String> mediaUrls;
    @NotEmpty
    private List<Platform> platforms;
    private Instant scheduledAt;
}
