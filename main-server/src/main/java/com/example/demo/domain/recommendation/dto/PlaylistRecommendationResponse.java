package com.example.demo.domain.recommendation.dto;

import java.util.List;

public record PlaylistRecommendationResponse(
        List<RecommendedPlaylistResponseDto> genreBased,
        List<RecommendedPlaylistResponseDto> likeBased
) {
    public static PlaylistRecommendationResponse of(
            List<RecommendedPlaylistResponseDto> genre,
            List<RecommendedPlaylistResponseDto> likes
    ) {
        return new PlaylistRecommendationResponse(genre, likes);
    }

    public static PlaylistRecommendationResponse onlyLikes(List<RecommendedPlaylistResponseDto> likes) {
        return new PlaylistRecommendationResponse(List.of(), likes);
    }
}
