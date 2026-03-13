package com.example.demo.domain.recommendation.dto.bundle;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record AddBundlePlaylistResponse(

        @Schema(description = "번들 ID")
        Long bundleId,

        @Schema(description = "추가된 플레이리스트 목록")
        List<BundlePlaylistResult> playlists
) {
    public record BundlePlaylistResult(

            @Schema(description = "플레이리스트 ID")
            Long playlistId,

            @Schema(description = "노출 순서")
            Integer orderIndex
    ) {
    }
}
