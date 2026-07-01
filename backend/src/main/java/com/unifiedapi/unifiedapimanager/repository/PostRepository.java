package com.unifiedapi.unifiedapimanager.repository;

import com.unifiedapi.unifiedapimanager.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {
    List<Post> findByUserIdOrderByCreatedAtDesc(UUID userId);

    @Query("SELECT p FROM Post p WHERE p.status = 'SCHEDULED' AND p.scheduledAt <= :now")
    List<Post> findDueScheduledPosts(@Param("now") Instant now);

    long countByUserId(UUID userId);
}
