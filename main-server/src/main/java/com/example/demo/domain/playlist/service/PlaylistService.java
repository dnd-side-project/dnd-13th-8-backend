package com.example.demo.domain.playlist.service;

import com.example.demo.domain.playlist.dto.PlaylistCreateRequest;
import com.example.demo.domain.playlist.dto.PlaylistDetailResponse;
import com.example.demo.domain.playlist.dto.PlaylistResponse;
import com.example.demo.domain.playlist.dto.PlaylistSortOption;
import com.example.demo.domain.playlist.dto.PlaylistWithSongsResponse;
import java.util.List;

public interface PlaylistService {

    PlaylistWithSongsResponse savePlaylistWithSongs(String users, PlaylistCreateRequest request, String theme);

    List<PlaylistResponse> getMyPlaylistsSorted(String userId, PlaylistSortOption sortOption);

    PlaylistDetailResponse getPlaylistDetail(String id, Long playlistId);

    void deletePlaylist(String userId, Long playlistId);

    String sharePlaylist(String userId, Long playlistId);

    void updateRepresentative(String userId, Long playlistId);
}
