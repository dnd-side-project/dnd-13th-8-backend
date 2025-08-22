package com.example.demo.domain.playlist.repository;

import com.example.demo.domain.playlist.dto.LikedPlaylistDto;
import com.example.demo.domain.playlist.dto.PlaylistDetailResponse;
import com.example.demo.domain.playlist.dto.PlaylistSearchResponse;
import com.example.demo.domain.playlist.dto.PlaylistSortOption;
import com.example.demo.domain.playlist.dto.search.PlaylistSearchDto;
import com.example.demo.domain.playlist.dto.search.UserSearchDto;
import com.example.demo.domain.playlist.entity.Playlist;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface PlaylistRecommendationRepositoryCustom {

    List<Playlist> findRecommendedPlaylistsByUser(String userId, int limit);

    List<LikedPlaylistDto> findLikedPlaylistsWithMeta(String userId, PlaylistSortOption sort, int limit);

    List<PlaylistDetailResponse> findPlaylistsWithSongsByCreatorId(String creatorId);

    List<PlaylistSearchDto> searchPlaylists(String query, PlaylistSortOption sort, Pageable pageable);

    List<UserSearchDto> searchUsersWithRepresentativePlaylist(String query);
}
