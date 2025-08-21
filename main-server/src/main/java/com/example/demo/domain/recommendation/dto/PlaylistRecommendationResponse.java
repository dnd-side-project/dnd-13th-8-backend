package com.example.demo.domain.recommendation.dto;

import java.util.List;

public record PlaylistRecommendationResponse(
        List<RecommendedPlaylistResponse> genreBased,
        List<RecommendedPlaylistResponse> likeBased
) {
    public static PlaylistRecommendationResponse of(
            List<RecommendedPlaylistResponse> genre,
            List<RecommendedPlaylistResponse> likes
    ) {
        return new PlaylistRecommendationResponse(genre, likes);
    }

    public static PlaylistRecommendationResponse onlyLikes(List<RecommendedPlaylistResponse> likes) {
        return new PlaylistRecommendationResponse(List.of(), likes);
    }
}
