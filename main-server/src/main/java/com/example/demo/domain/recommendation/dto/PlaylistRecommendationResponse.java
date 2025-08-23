package com.example.demo.domain.recommendation.dto;

import com.example.demo.domain.playlist.dto.search.PlaylistSearchDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "플레이리스트 추천 응답")
public record PlaylistRecommendationResponse(

        @Schema(description = "최근 들은 장르 기반 추천 플레이리스트 목록")
        List<PlaylistSearchDto> genreBased,

        @Schema(description = "조회수 기반 추천 플레이리스트 목록")
        List<PlaylistSearchDto> likeBased
) {
    public static PlaylistRecommendationResponse of(
            List<PlaylistSearchDto> genre,
            List<PlaylistSearchDto> likes
    ) {
        return new PlaylistRecommendationResponse(genre, likes);
    }

    public static PlaylistRecommendationResponse onlyLikes(List<PlaylistSearchDto> likes) {
        return new PlaylistRecommendationResponse(List.of(), likes);
    }
}
