package com.example.demo.domain.playlist.dto;

import com.example.demo.domain.playlist.entity.Playlist;
import java.util.List;

public record PlaylistDetailResponse(
        Long id,
        String name,
        boolean isRepresentative,
        List<SongDto> tracks
) {
    public static PlaylistDetailResponse from(Playlist playlist, List<SongDto> tracks) {
        return new PlaylistDetailResponse(
                playlist.getId(),
                playlist.getName(),
                playlist.getIsRepresentative(),
                tracks
        );
    }
}
