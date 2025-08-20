package com.example.demo.domain.playlist.dto;

import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.user.entity.Users;

public class PlaylistMapper {

    public static Playlist toEntity(PlaylistCreateRequest request, String theme, Users users,boolean isRepresentative) {
        return Playlist.builder()
                .name(request.name())
                .genre(request.genre())
                .isRepresentative(isRepresentative)
                .theme(theme)
                .users(users)
                .build();
    }
}
