package com.example.demo.domain.playlist.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "플레이리스트 좋아요 응답")
public record PlaylistLikeResponse(

        @Schema(description = "현재 좋아요 여부", example = "true")
        boolean liked

) {}
