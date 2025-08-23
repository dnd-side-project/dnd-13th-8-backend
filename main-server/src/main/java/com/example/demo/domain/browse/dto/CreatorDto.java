package com.example.demo.domain.browse.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "플레이리스트 생성자 정보")
public record CreatorDto(

        @Schema(description = "유저 ID", example = "user-123")
        String ownerUserId,

        @Schema(description = "유저 닉네임", example = "음악감상러")
        String ownerUsername
) {}
