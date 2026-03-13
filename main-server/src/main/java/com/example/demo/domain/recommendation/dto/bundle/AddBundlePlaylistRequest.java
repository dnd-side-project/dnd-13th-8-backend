package com.example.demo.domain.recommendation.dto.bundle;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record AddBundlePlaylistRequest(
        @Schema(description = "추가할 플레이리스트 목록")
        List<BundlePlaylistItem> playlists
) {
    public record BundlePlaylistItem(

            @Schema(description = "플레이리스트 ID")
            Long playlistId,

            @Schema(description = "순서")
            Integer orderIndex

    ) {
    }

}
