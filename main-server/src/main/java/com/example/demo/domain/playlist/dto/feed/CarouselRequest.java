package com.example.demo.domain.playlist.dto.feed;

import com.example.demo.domain.playlist.dto.common.PlaylistSortOption;
import com.example.demo.domain.validation.annotation.ValidCarouselRequest;
import io.swagger.v3.oas.annotations.media.Schema;

@ValidCarouselRequest
public record CarouselRequest(
        @Schema(description = "기준 플레이리스트 ID (초기 조회 시 사용)", example = "10")
        Long anchorId,

        @Schema(description = "추가 로딩 방향", example = "NEXT/PREV")
        CarouselDirection direction,

        @Schema(description = "커서 플레이리스트 ID", example = "21")
        Long cursor,

        @Schema(description = "정렬 옵션", example = "POPULAR/RECENT")
        PlaylistSortOption sort,

        @Schema(description = "조회 개수", example = "3", defaultValue = "3")
        Integer limit
) {
}
