package com.example.demo.domain.songs.dto;

public record SongResponseDto(
        Long id,
        Long playlistId,
        String youtubeUrl,
        String youtubeTitle,
        String youtubeThumbnail,
        Long youtubeLength, // 초 단위
        Long orderIndex
) {}
