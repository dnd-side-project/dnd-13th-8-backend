package com.example.demo.domain.recommendation.dto;

import com.example.demo.domain.playlist.dto.SongDto;
import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.song.entity.Song;
import com.example.demo.domain.user.entity.Users;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "플레이리스트 카드 응답")
public record PlaylistCardResponse(

        @Schema(description = "플레이리스트 ID", example = "101")
        Long playlistId,

        @Schema(description = "플레이리스트 이름", example = "비 오는 날 듣기 좋은 발라드")
        String playlistName,

        @Schema(description = "플레이리스트 제작자 ID", example = "user-123")
        String creatorId,

        @Schema(description = "플레이리스트 제작자 닉네임", example = "playlist_maker")
        String creatorNickname,

        @Schema(description = "추천된 곡 목록")
        List<SongDto> songs

) {
        public static PlaylistCardResponse from(Playlist playlist, List<Song> songs) {
                Users owner = playlist.getUsers(); // 단방향 ManyToOne은 유지됨

                List<SongDto> songDtos = songs.stream()
                        .map(SongDto::from)
                        .toList();

                return new PlaylistCardResponse(
                        playlist.getId(),
                        playlist.getName(),
                        owner.getId(),
                        owner.getUsername(),
                        songDtos
                );
        }

}

