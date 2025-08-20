package com.example.demo.domain.playlist.dto;

import com.example.demo.domain.song.dto.SongResponseDto;
import java.util.List;

public record PlaylistWithSongsResponse(
        Long playlistId,
        List<SongResponseDto> songs
) {}
