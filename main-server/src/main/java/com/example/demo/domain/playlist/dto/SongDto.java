package com.example.demo.domain.playlist.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.example.demo.domain.song.entity.Song;
import com.example.demo.domain.song.util.DurationFormatUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

@Schema(description = "간략한 곡 정보 DTO")
@Builder
public record SongDto(

        @Schema(description = "곡 ID", example = "501")
        Long id,

        @Schema(description = "유튜브 영상 제목", example = "비 오는 날 듣기 좋은 노래")
        String title,

        @Schema(description = "유튜브 영상 URL", example = "https://youtube.com/watch?v=abc123")
        String youtubeUrl,

        @Schema(description = "썸네일 이미지 URL", example = "https://img.youtube.com/vi/abc123/hqdefault.jpg")
        String youtubeThumbnail,

        @Schema(description = "유튜브 영상 길이", example = "SS")
        Long youtubeLength,

        @Schema(description = "곡 순서", example = "1")
        Long orderIndex


) {
    public static SongDto from(Song song) {
        return SongDto.builder()
                .id(song.getId())
                .title(song.getYoutubeTitle())
                .youtubeUrl(song.getYoutubeUrl())
                .youtubeThumbnail(song.getYoutubeThumbnail())
                .youtubeLength(song.getYoutubeLength())
                .orderIndex(song.getOrderIndex())
                .build();
    }

    public static List<SongDto> fromJsonList(String songsJson) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(songsJson, new TypeReference<>() {});
        } catch (Exception e) {
            throw new RuntimeException("type 으로 변환 중 오루", e);
        }
    }
}
