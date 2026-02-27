package com.example.demo.domain.playlist.dto.search;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "SearchResponse", description = "통합 검색 페이지 응답 (스웨거 조회용)")
public record SearchResponse(

        @ArraySchema(
                schema = @Schema(oneOf = {UserSearchDto.class, PlaylistSearchDto.class}),
                arraySchema = @Schema(description = "검색 결과 목록(Playlist 또는 User)")
        )
        List<SearchItem> content,

        int page,
        int size,
        boolean hasNext,
        long totalCount
) {}
