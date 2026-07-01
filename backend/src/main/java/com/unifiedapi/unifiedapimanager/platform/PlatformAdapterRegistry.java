package com.unifiedapi.unifiedapimanager.platform;

import com.unifiedapi.unifiedapimanager.model.Platform;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PlatformAdapterRegistry {

    private final Map<Platform, PlatformAdapter> adapters;

    public PlatformAdapterRegistry(List<PlatformAdapter> adapterList) {
        this.adapters = adapterList.stream()
                .collect(Collectors.toMap(PlatformAdapter::getPlatform, Function.identity()));
    }

    public Optional<PlatformAdapter> getAdapter(Platform platform) {
        return Optional.ofNullable(adapters.get(platform));
    }

    public List<Platform> supportedPlatforms() {
        return List.copyOf(adapters.keySet());
    }
}
