package com.example.demo.domain.playlist.service;

import com.example.demo.domain.playlist.dto.common.PlaylistCoverResponse;
import com.example.demo.domain.playlist.dto.common.PlaylistSortOption;
import com.example.demo.global.paging.CursorPageResponse;

public interface PlaylistFeedService {
    CursorPageResponse<PlaylistCoverResponse, String> getPlaylistsSorted(
            String shareCode,
            String meId,
            PlaylistSortOption sortOption,
            String opaqueCursor,
            int limit
    );

    CursorPageResponse<PlaylistCoverResponse, String> getLikedPlaylistsSorted(
            String shareCode,
            String meId,
            PlaylistSortOption sortOption,
            String opaqueCursor,
            int limit
    );
}
