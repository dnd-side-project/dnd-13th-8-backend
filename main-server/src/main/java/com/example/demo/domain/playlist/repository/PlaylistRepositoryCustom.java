package com.example.demo.domain.playlist.repository;

import com.example.demo.domain.playlist.dto.common.PlaylistGenre;
import com.example.demo.domain.playlist.dto.common.PlaylistSortOption;
import com.example.demo.domain.playlist.dto.search.PlaylistSearchDto;
import com.example.demo.domain.playlist.dto.search.SearchResult;
import com.example.demo.domain.playlist.entity.Playlist;
import java.util.List;

public interface PlaylistRepositoryCustom {
    List<Long> findFollowedPlaylistIds(String currentUserId);
    List<Playlist> findPlaylistsBySimilarSongs(List<Long> basePlaylistIds, String excludeUserId, List<Long> excludePlaylistIds, int limit);
    List<Playlist> findLatestPlaylists(String excludeUserId, List<Long> excludePlaylistIds, int limit);
    SearchResult<Playlist> findByGenreWithCursor(PlaylistGenre genre, PlaylistSortOption sort, Long cursorId, int limit);
    SearchResult<PlaylistSearchDto> searchPlaylistsByTitleWithOffset(String query, PlaylistSortOption sort, int offset, int limit);
    List<Playlist> findByVisitCount(int limit);
    List<Playlist> findByUserIdSorted(String userId, PlaylistSortOption sort);
}
