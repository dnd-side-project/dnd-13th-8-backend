package com.example.demo.domain.playlist.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "장르 정보 DTO")
public record GenreDto(

        @Schema(description = "장르 코드 (enum 이름)", example = "STUDY")
        String code,

        @Schema(description = "장르 표시 이름 (사용자에게 노출용)", example = "공부·집중")
        String displayName

) {}
