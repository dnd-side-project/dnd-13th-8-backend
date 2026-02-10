package com.example.demo.domain.playlist.dto;

import com.example.demo.domain.playlist.dto.playlistdto.SavePlaylistRequest;
import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.user.entity.Users;

public class PlaylistMapper {

    public static Playlist toEntity(SavePlaylistRequest request, Users users) {
        return Playlist.builder()
                .name(request.name())
                .genre(request.genre())
                .isPublic(request.isPublic())
                .users(users)
                .build();
    }
}
