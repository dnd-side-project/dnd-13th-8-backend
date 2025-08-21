package com.example.demo.domain.recommendation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "좋아요 기반 추천 플레이리스트 응답")
public record RecommendedPlaylistsWithSongsResponse(

        @Schema(description = "추천된 플레이리스트 카드 목록")
        List<RecommendedPlaylistCard> recommendations

) {}
