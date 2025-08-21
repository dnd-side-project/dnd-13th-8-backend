package com.example.demo.domain.recommendation.dto;

import com.example.demo.domain.playlist.dto.SongDto;
import java.util.List;

public record RecommendedPlaylistCard(
        Long playlistId,
        String playlistName,
        String ownerName,
        boolean isRepresentative,
        List<SongDto> songs
) {
}
