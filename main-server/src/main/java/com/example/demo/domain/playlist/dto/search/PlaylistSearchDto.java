package com.example.demo.domain.playlist.dto.search;

import com.example.demo.domain.cd.dto.response.OnlyCdResponse;
import com.example.demo.domain.playlist.dto.SongDto;
import com.example.demo.domain.playlist.entity.Playlist;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "검색 결과 중 플레이리스트 항목")
public record PlaylistSearchDto(

        @Schema(description = "플레이리스트 ID", example = "1001")
        Long playlistId,

        @Schema(description = "플레이리스트 이름", example = "감성 힙합 모음집")
        String playlistName,

        @Schema(description = "플레이리스트 제작자 ID", example = "user-123")
        String creatorId,

        @Schema(description = "플레이리스트 제작자 닉네임", example = "playlist_maker")
        String creatorNickname,

        @Schema(description = "cd 정보")
        OnlyCdResponse onlyCdResponse

) implements SearchItem {

    @Override
    @Schema(description = "검색 결과 타입", example = "PLAYLIST")
    public String getType() {
        return "PLAYLIST";
    }

    public static PlaylistSearchDto from(
            Long playlistId,
            String playlistName,
            String creatorId,
            String creatorNickname,
            OnlyCdResponse onlyCdResponse
    ) {
        return new PlaylistSearchDto(
                playlistId,
                playlistName,
                creatorId,
                creatorNickname,
                onlyCdResponse
        );
    }
}
