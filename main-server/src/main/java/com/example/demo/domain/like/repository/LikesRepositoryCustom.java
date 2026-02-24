package com.example.demo.domain.like.repository;

import com.example.demo.domain.playlist.dto.common.PlaylistSortOption;
import com.example.demo.domain.playlist.entity.Playlist;
import java.util.List;

public interface LikesRepositoryCustom {
    List<Long> findLikedPlaylistIdsIn(String userId, List<Long> playlistIds);
    List<Playlist> findLikedPlaylistsWithMeta(String userId, PlaylistSortOption sort, int limit);
}