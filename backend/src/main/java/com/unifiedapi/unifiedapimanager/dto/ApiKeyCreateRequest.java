package com.unifiedapi.unifiedapimanager.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ApiKeyCreateRequest {
    @NotBlank
    private String name;
}
