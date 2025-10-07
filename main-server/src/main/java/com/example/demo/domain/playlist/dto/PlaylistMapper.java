package com.example.demo.domain.playlist.dto;

import com.example.demo.domain.playlist.dto.playlistdto.PlaylistCreateRequest;
import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.user.entity.Users;

public class PlaylistMapper {

    public static Playlist toEntity(PlaylistCreateRequest request, Users users) {
        return Playlist.builder()
                .name(request.name())
                .genre(request.genre())
                .isPulic(request.isPublic())
                .users(users)
                .build();
    }
}
