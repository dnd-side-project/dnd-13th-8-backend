package com.example.demo.domain.recommendation.dto;

import com.example.demo.domain.recommendation.entity.bundle.BundleTimeSlot;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "시간대별 모음집 추천 응답")
public record GetTimeRecommendationResponse(
        @Schema(description = "모음집 ID", example = "1")
        Long bundleId,

        @Schema(description = "모음집 제목")
        String title,

        @Schema(description = "시간대", example = "DAWN/MORNING/AFTERNOON/EVENING")
        BundleTimeSlot timeSlot,

        @Schema(description = "플레이리스트")
        List<RecommendedPlaylistResponse> playlists

) {}