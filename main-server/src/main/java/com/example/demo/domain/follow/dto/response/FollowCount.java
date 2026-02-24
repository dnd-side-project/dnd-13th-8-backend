package com.example.demo.domain.follow.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "팔로우/팔로잉 수")
public record FollowCount(long followerCount, long followingCount) {
}
