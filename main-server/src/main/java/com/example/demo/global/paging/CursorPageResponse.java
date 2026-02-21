package com.example.demo.global.paging;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "커서 기반 페이지네이션 응답")
public record CursorPageResponse<T, C>(

        @ArraySchema(schema = @Schema(description = "조회된 콘텐츠 목록"))
        List<T> content,

        @Schema(description = "다음 페이지 요청에 사용할 커서 (도메인별 구조체)", nullable = true)
        C nextCursor,

        @Schema(description = "현재 페이지 항목 수", example = "20")
        int size,

        @Schema(description = "다음 페이지 존재 여부", example = "true")
        boolean hasNext,

        @Schema(description = "총 검색 결과 수", example ="22")
        long totalCount
) {
}
