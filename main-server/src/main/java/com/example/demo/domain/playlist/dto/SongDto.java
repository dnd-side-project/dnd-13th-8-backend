package com.example.demo.domain.playlist.dto;

import com.example.demo.domain.song.entity.Song;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "간략한 곡 정보 DTO")
@Builder
public record SongDto(

        @Schema(description = "곡 ID", example = "501")
        Long id,

        @Schema(description = "유튜브 영상 제목", example = "비 오는 날 듣기 좋은 노래")
        String title,

        @Schema(description = "유튜브 영상 URL", example = "https://youtube.com/watch?v=abc123")
        String youtubeUrl

) {
    public static SongDto from(Song song) {
        return SongDto.builder()
                .id(song.getId())
                .title(song.getYoutubeTitle())
                .youtubeUrl(song.getYoutubeUrl())
                .build();
    }
}
