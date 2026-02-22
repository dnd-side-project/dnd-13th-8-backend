package com.example.demo.domain.follow.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "FollowListResponse", description = "팔로우 목록 커서 페이지 응답 (스웨거 조회용)")
public record FollowListResponse(
        @Schema(description = "조회된 콘텐츠 목록")
        List<FollowListItem> content,

        @Schema(description = "다음 페이지 요청에 사용할 커서", nullable = true)
        Long nextCursor,

        @Schema(description = "현재 페이지 항목 수", example = "20")
        int size,

        @Schema(description = "다음 페이지 존재 여부", example = "true")
        boolean hasNext,

        @Schema(description = "총 검색 결과 수", example = "22")
        long totalCount
) {}