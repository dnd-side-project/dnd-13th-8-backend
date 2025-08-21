package com.example.demo.domain.recommendation.dto;

import java.time.LocalDateTime;

public record PlaylistRecommendationDto(
        Long id,
        String name,
        String ownerName,
        int likeCount,
        LocalDateTime createdAt
) {}

