package com.example.demo.domain.playlist.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "플레이리스트 정렬 옵션")
public enum PlaylistSortOption {

    @Schema(description = "인기순 (조회수 기준)")
    POPULAR,

    @Schema(description = "최신순 (생성일 기준)")
    RECENT
}
