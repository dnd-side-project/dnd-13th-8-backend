package com.example.demo.domain.like.repository;

import com.example.demo.domain.playlist.dto.PlaylistSortOption;
import com.example.demo.domain.playlist.entity.Playlist;
import java.util.List;

public interface LikesRepositoryCustom {
    List<Playlist> findLikedPlaylistsWithMeta(String userId, PlaylistSortOption sort, int limit);
}