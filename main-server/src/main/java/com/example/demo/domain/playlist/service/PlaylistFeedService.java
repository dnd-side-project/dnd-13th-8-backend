package com.example.demo.domain.playlist.service;

import com.example.demo.domain.playlist.dto.common.PlaylistCoverResponse;
import com.example.demo.domain.playlist.dto.common.PlaylistSortOption;
import com.example.demo.global.paging.CursorPageResponse;

public interface PlaylistFeedService {
    CursorPageResponse<PlaylistCoverResponse, Long> getPlaylistsSorted(
            String shareCode,
            String meId,
            PlaylistSortOption sortOption,
            Long cursor,
            int limit
    );

    CursorPageResponse<PlaylistCoverResponse, Long> getLikedPlaylistsSorted(
            String shareCode,
            String meId,
            PlaylistSortOption sortOption,
            Long cursor,
            int limit
    );
}
