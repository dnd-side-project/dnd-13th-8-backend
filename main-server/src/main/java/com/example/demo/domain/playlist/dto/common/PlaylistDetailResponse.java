package com.example.demo.domain.playlist.dto.common;

import com.example.demo.domain.cd.dto.response.CdResponse;
import com.example.demo.domain.playlist.entity.Playlist;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "플레이리스트 상세 응답 DTO")
public record PlaylistDetailResponse(

        @Schema(description = "플레이리스트 ID", example = "101")
        Long playlistId,

        @Schema(description = "플레이리스트 이름", example = "집중할 때 듣는 음악")
        String playlistName,

        @Schema(description = "공개 여부", example = "true")
        boolean isPublic,

        @Schema(description = "플레이리스트에 포함된 곡 목록")
        List<SongDto> songs,

        @Schema(description = "플레이리스트 장르", example = "JAZZ")
        PlaylistGenre genre,

        @Schema(description = "플레이리스트에 포함된 CD 아이템 목록")
        CdResponse cdResponse
) {
    public static PlaylistDetailResponse from(Playlist playlist, List<SongDto> songs, CdResponse cdResponse) {
        return new PlaylistDetailResponse(
                playlist.getId(),
                playlist.getName(),
                playlist.isPublic(),
                songs,
                playlist.getGenre(),
                cdResponse

        );
    }
}
