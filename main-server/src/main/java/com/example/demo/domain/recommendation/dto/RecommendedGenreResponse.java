package com.example.demo.domain.recommendation.dto;

import com.example.demo.domain.playlist.dto.PlaylistGenre;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "추천 장르 응답 DTO")
public record RecommendedGenreResponse(

        @Schema(description = "추천된 장르", example = "JAZZ")
        PlaylistGenre genre

) {
    public static RecommendedGenreResponse from(PlaylistGenre genre) {
        return new RecommendedGenreResponse(genre);
    }
}
