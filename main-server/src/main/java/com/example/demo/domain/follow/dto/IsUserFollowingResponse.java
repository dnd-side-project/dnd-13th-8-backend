package com.example.demo.domain.follow.dto;

import lombok.Builder;

@Builder
public record IsUserFollowingResponse (boolean isFollowing) {
}
