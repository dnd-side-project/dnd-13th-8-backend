package com.example.demo.domain.cd.dto.response;

import java.util.List;
import java.util.Map;

public record CdItemsByPlaylist(
        Map<Long, List<CdItem>> itemsByPlaylistId
) {
    public static CdItemsByPlaylist empty() {
        return new CdItemsByPlaylist(Map.of());
    }

    public CdResponse cdItemsOf(Long playlistId) {
        return CdResponse.of(itemsByPlaylistId.getOrDefault(playlistId, List.of()));
    }
}
