package com.example.demo.domain.song.dto;

public record SongResponseDto(
        Long id,
        com.example.demo.domain.playlist.entity.Playlist playlistId,
        String youtubeUrl,
        String youtubeTitle,
        String youtubeThumbnail,
        Long youtubeLength // 초 단위
) {}
