package com.example.demo.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "프로필 변경 응답 DTO")
public record UpdateProfileResponse(

        String userId,

        @Schema(description = "변경된 닉네임", example = "jun_dev")
        String nickname,

        @Schema(description = "프로필 이미지 Presigned URL")
        String profileImageUrl
) {}
