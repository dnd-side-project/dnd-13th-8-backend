package com.example.demo.domain.recommendation.dto.bundle;

import com.example.demo.domain.recommendation.entity.bundle.BundleTimeSlot;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record GetAllBundlesResponse(

        @Schema(description = "모음집 ID")
        Long bundleId,

        @Schema(description = "시간대")
        BundleTimeSlot timeSlot,

        @Schema(description = "모음집 제목")
        String title,

        @Schema(description = "모음집에 포함된 플레이리스트 목록")
        List<BundlePlaylistItem> playlists

) {
    public record BundlePlaylistItem(

            @Schema(description = "플레이리스트 ID")
            Long playlistId,

            @Schema(description = "플레이리스트 제목")
            String playlistName

    ) {
    }
}
