package com.example.demo.domain.playlist.service;

import com.example.demo.domain.playlist.dto.common.PlaylistCoverResponse;
import com.example.demo.domain.playlist.dto.common.PlaylistSortOption;
import com.example.demo.domain.playlist.dto.feed.CarouselDirection;
import com.example.demo.global.paging.BiCursorPageResponse;

public interface PlaylistCarouselService {
    BiCursorPageResponse<PlaylistCoverResponse, Long> getFeedCarouselAround(
            String shareCode,
            String meId,
            PlaylistSortOption sort,
            Long anchorId,
            int limit
    );

    BiCursorPageResponse<PlaylistCoverResponse, Long> getFeedCarouselMore(
            String shareCode,
            String meId,
            PlaylistSortOption sort,
            CarouselDirection direction,
            Long cursor,
            int limit
    );

    BiCursorPageResponse<PlaylistCoverResponse, Long> getLikedCarouselAround(
            String shareCode,
            String meId,
            PlaylistSortOption sort,
            Long anchorId,
            int limit
    );

    BiCursorPageResponse<PlaylistCoverResponse, Long> getLikedCarouselMore(
            String shareCode,
            String meId,
            PlaylistSortOption sort,
            CarouselDirection direction,
            Long cursor,
            int limit
    );
}
