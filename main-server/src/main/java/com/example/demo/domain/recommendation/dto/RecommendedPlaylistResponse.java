package com.example.demo.domain.recommendation.dto;

import com.example.demo.domain.playlist.dto.PlaylistGenre;
import com.querydsl.core.annotations.QueryProjection;

public record RecommendedPlaylistResponse(
        Long playlistId,
        String title,
        String nickName,
        PlaylistGenre genres,
        Long visitCount
) {
    @QueryProjection
    public RecommendedPlaylistResponse {}
}
