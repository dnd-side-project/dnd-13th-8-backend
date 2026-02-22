package com.example.demo.domain.follow.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public enum FollowSortOption {
    @Schema(description = "최신순")
    LATEST,

    @Schema(description = "오래된순")
    OLDEST
}
