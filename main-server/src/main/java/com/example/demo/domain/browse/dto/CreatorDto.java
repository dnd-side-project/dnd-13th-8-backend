package com.example.demo.domain.browse.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "플레이리스트 생성자 정보")
public record CreatorDto(

        @Schema(description = "플레이리스트 제작자 ID", example = "user-123")
        String creatorId,

        @Schema(description = "플레이리스트 제작자 닉네임", example = "playlist_maker")
        String creatorNickname

) {}