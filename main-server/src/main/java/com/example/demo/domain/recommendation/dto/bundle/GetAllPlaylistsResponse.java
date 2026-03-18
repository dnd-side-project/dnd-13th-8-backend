package com.example.demo.domain.recommendation.dto.bundle;

import com.example.demo.domain.playlist.dto.common.PlaylistGenre;
import com.example.demo.domain.playlist.entity.Playlist;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record GetAllPlaylistsResponse(List<PlaylistItem> playlists) {

    public static GetAllPlaylistsResponse from(List<Playlist> playlists) {
        return new GetAllPlaylistsResponse(
                playlists.stream()
                        .map(PlaylistItem::from)
                        .toList()
        );
    }

    public record PlaylistItem(

            @Schema(description = "플레이리스트 ID")
            Long playlistId,

            @Schema(description = "플레이리스트 제목")
            String playlistName,

            @Schema(description = "플레이리스트 장르")
            PlaylistGenre playlistGenre

    ) {
        public static PlaylistItem from(Playlist playlist) {
            return new PlaylistItem(
                    playlist.getId(),
                    playlist.getName(),
                    playlist.getGenre()
            );
        }
    }
}
