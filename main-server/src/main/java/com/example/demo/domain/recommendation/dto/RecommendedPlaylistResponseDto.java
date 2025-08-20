package com.example.demo.domain.recommendation.dto;

import com.example.demo.domain.playlist.dto.PlaylistGenre;
import com.querydsl.core.annotations.QueryProjection;

public record RecommendedPlaylistResponseDto(
        Long playlistId,
        String title,
        String nickaName,
        PlaylistGenre genres,
        Long visitCount
) {
    @QueryProjection
    public RecommendedPlaylistResponseDto {}
}
