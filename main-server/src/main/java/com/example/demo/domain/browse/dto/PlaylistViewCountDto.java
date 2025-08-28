package com.example.demo.domain.browse.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "플레이리스트 조회수 DTO")
public record PlaylistViewCountDto(
        @Schema(description = "플레이리스트 ID", example = "101")
        Long playlistId,

        @Schema(description = "조회수", example = "15")
        Long viewCount
) {}
