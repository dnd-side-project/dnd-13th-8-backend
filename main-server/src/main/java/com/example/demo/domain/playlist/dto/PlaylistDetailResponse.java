package com.example.demo.domain.playlist.dto;

import com.example.demo.domain.playlist.entity.Playlist;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "플레이리스트 상세 응답 DTO")
public record PlaylistDetailResponse(

        @Schema(description = "플레이리스트 ID", example = "101")
        Long id,

        @Schema(description = "플레이리스트 이름", example = "집중할 때 듣는 음악")
        String name,

        @Schema(description = "대표 플레이리스트 여부", example = "true")
        boolean isRepresentative,

        @Schema(description = "플레이리스트에 포함된 곡 목록")
        List<SongDto> tracks,

        @Schema(description = "플레이리스트 장르", example = "JAZZ")
        PlaylistGenre genre
) {
    public static PlaylistDetailResponse from(Playlist playlist, List<SongDto> tracks) {
        return new PlaylistDetailResponse(
                playlist.getId(),
                playlist.getName(),
                playlist.isRepresentative(),
                tracks,
                playlist.getGenre()
        );
    }
}
