package com.example.demo.domain.recommendation.dto;

import com.example.demo.domain.playlist.dto.PlaylistGenre;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "추천 장르 응답 DTO")
public record RecommendedGenreResponse(

        @Schema(description = "장르 코드", example = "JAZZ")
        String code,

        @Schema(description = "장르 표시 이름", example = "기분전환")
        String displayName

) {
    public static RecommendedGenreResponse from(PlaylistGenre genre) {
        return new RecommendedGenreResponse(
                genre.name(),            // code
                genre.getDisplayName()  // displayName
        );
    }
}
