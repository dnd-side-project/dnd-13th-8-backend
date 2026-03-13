package com.example.demo.domain.recommendation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "인기 있는 유저 추천 응답")
public record RecommendedUserResponse(
        @Schema(description = "유저 ID", example = "1")
        String userId,

        @Schema(description = "유저 닉네임", example = "지구젤리")
        String nickname,

        @Schema(description = "유저 프로필 이미지", example = "www.example.com")
        String profileUrl,

        @Schema(description = "유저 프로필 아이디", example = "jigu_jelly")
        String shareCode) {
}
