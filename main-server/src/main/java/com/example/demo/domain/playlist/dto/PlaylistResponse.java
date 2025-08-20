package com.example.demo.domain.playlist.dto;

import com.example.demo.domain.playlist.entity.Playlist;
import lombok.Builder;

@Builder
public record PlaylistResponse(
        Long id,
        String name,
        boolean isRepresentative,
        Long likeCount
) {
    public static PlaylistResponse from(Playlist playlist) {
        return PlaylistResponse.builder()
                .id(playlist.getId())
                .name(playlist.getName())
                .isRepresentative(playlist.getIsRepresentative())
                .likeCount(playlist.getVisitCount())
                .build();
    }
}
