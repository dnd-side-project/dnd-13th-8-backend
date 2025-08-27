package com.example.demo.domain.playlist.dto.playlistdto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "오프셋 기반 페이지 응답")
public record PageResponse<T>(
        @Schema(description = "조회된 콘텐츠 목록")
        T content,

        @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
        int page,

        @Schema(description = "요청한 페이지 크기", example = "10")
        int size,

        @Schema(description = "다음 페이지 존재 여부", example = "true")
        boolean hasNext
) {}
