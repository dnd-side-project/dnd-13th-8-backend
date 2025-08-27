package com.example.demo.domain.playlist.dto.playlistdto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "커서 기반 페이지 응답")
public record CursorPageResponse<T>(
        @Schema(description = "조회된 콘텐츠 목록")
        List<T> content,

        @Schema(description = "다음 페이지 요청에 사용할 커서 (마지막 ID)", example = "123")
        String nextCursor,

        @Schema(description = "현재 페이지 항목 수", example = "10")
        int size,

        @Schema(description = "다음 페이지 존재 여부", example = "true")
        boolean hasNext
) {}

