package com.example.demo.domain.playlist.dto.feed;

import com.example.demo.domain.playlist.dto.common.PlaylistCoverResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(
        name = "FeedPlaylistListResponse",
        description = "플레이리스트 커서 기반 페이지 응답 (스웨거 조회용)"
)
public record FeedPlaylistListResponse(

        @Schema(description = "조회된 플레이리스트 목록")
        List<PlaylistCoverResponse> content,

        @Schema(description = "다음 페이지 요청에 사용할 커서 (opaque string)", nullable = true)
        String nextCursor,

        @Schema(description = "현재 페이지 항목 수", example = "20")
        int size,

        @Schema(description = "다음 페이지 존재 여부", example = "true")
        boolean hasNext,

        @Schema(description = "총 검색 결과 수", example = "42")
        long totalCount
) {}
