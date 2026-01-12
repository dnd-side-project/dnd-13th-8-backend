package com.example.demo.domain.song.dto.api;

import com.example.demo.domain.song.util.DurationFormatUtil;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "유튜브 영상 API DTO")
public record YouTubeApiVideoDto(

        @Schema(description = "입력한 유튜브 링크", example = "https://youtu.be/abc123")
        String link,

        @Schema(description = "영상 제목", example = "집중용 Lofi 음악")
        String title,

        @Schema(description = "썸네일 URL", example = "https://img.youtube.com/vi/abc123/hqdefault.jpg")
        String thumbnailUrl,

        @Schema(description = "영상 길이 (MM:SS)", example = "03:21")
        String duration,

        @Schema(description = "곡 순서", example = "1")
        Long orderIndex,

        @Schema(description = "유효한 영상 여부", example = "true")
        boolean valid


) {
    public static YouTubeApiVideoDto valid(String link, String title, String thumbnailUrl, Long orderIndex, String duration) {
        return new YouTubeApiVideoDto(link, title, thumbnailUrl, DurationFormatUtil.formatDuration(duration),orderIndex,true);
    }

    public static YouTubeApiVideoDto invalid(String link) {
        return new YouTubeApiVideoDto(link, null, null, null, null, false);
    }
}
