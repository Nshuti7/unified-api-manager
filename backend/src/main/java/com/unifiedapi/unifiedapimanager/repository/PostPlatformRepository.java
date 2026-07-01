package com.unifiedapi.unifiedapimanager.repository;

import com.unifiedapi.unifiedapimanager.model.PostPlatform;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PostPlatformRepository extends JpaRepository<PostPlatform, UUID> {
}
