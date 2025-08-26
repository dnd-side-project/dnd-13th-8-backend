package com.example.demo.domain.playlist.repository;

import com.example.demo.domain.playlist.entity.Playlist;
import java.util.List;

public interface PlaylistRepositoryCustom {
    List<Long> findFollowedRepresentativePlaylistIds(String currentUserId);
    List<Playlist> findPlaylistsBySimilarSongs(List<Long> basePlaylistIds, String excludeUserId, List<Long> excludePlaylistIds, int limit);
    List<Playlist> findLatestRepresentativePlaylists(String excludeUserId, List<Long> excludePlaylistIds, int limit);
}
