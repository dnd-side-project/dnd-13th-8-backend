package com.example.demo.domain.follow.dto.response;

import lombok.Builder;

@Builder
public record IsUserFollowingResponse (boolean isFollowing) {
}
