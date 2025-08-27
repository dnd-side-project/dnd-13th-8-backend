package com.example.demo.domain.playlist.dto.search;
import java.util.List;

public record PopularSearchResponse(
        String range,                     // 조회 범위 (예: "today", "7d", "30d")
        int limit,                        // 요청한 최대 개수
        List<PopularItem> keywords       // 인기 검색어 리스트
) {}
