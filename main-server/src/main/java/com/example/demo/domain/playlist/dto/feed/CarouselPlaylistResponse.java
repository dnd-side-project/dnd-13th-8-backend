package com.example.demo.domain.playlist.dto.feed;

import com.example.demo.domain.playlist.dto.common.PlaylistCoverResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record CarouselPlaylistResponse(
        @Schema(description = "조회된 캐러셀 목록 (스웨거 조회용)")
        List<PlaylistCoverResponse> content,

        @Schema(description = "이전(왼쪽) 페이지 요청에 사용할 커서", nullable = true)
        String prevCursor,

        @Schema(description = "다음(오른쪽) 페이지 요청에 사용할 커서", nullable = true)
        String nextCursor,

        @Schema(description = "현재 페이지 항목 수", example = "21")
        int size,

        @Schema(description = "이전(왼쪽) 페이지 존재 여부", example = "true")
        boolean hasPrev,

        @Schema(description = "다음(오른쪽) 페이지 존재 여부", example = "true")
        boolean hasNext,

        @Schema(description = "총 검색 결과 수", example = "42")
        long totalCount
) {
}
