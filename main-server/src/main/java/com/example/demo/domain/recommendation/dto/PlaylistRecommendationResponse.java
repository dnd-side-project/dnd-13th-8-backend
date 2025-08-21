package com.example.demo.domain.recommendation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "플레이리스트 추천 응답")
public record PlaylistRecommendationResponse(

        @Schema(description = "최근 들은 장르 기반 추천 플레이리스트 목록")
        List<RecommendedPlaylistResponse> genreBased,

        @Schema(description = "조회수 기반 추천 플레이리스트 목록")
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
