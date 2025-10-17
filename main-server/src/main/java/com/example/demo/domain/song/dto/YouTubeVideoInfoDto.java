package com.example.demo.domain.song.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "유튜브 영상 정보 DTO")
public record YouTubeVideoInfoDto(

        @Schema(description = "사용자가 입력한 유튜브 링크", example = "https://www.youtube.com/watch?v=abc123")
        String link,

        @Schema(description = "유튜브 영상 제목", example = "Lofi Jazz - 집중용 음악")
        String title,

        @Schema(description = "유튜브 썸네일 URL", example = "https://img.youtube.com/vi/abc123/hqdefault.jpg")
        String thumbnailUrl,

        @Schema(description = "영상 길이 (MM:SS)", example = "03:22")
        String duration,

        @Schema(description = "곡 순서", example = "1")
        Long orderIndex

) {}
