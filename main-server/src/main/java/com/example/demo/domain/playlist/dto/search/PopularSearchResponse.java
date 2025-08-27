package com.example.demo.domain.playlist.dto.search;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "인기 검색어 응답")
public record PopularSearchResponse(
        @Schema(description = "조회 범위", example = "today")
        String range,

        @Schema(description = "조회 개수", example = "10")
        int limit,

        @Schema(description = "인기 검색어 리스트")
        List<PopularItem> keywords
) {}
