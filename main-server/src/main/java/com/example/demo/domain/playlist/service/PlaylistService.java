package com.example.demo.domain.playlist.service;

import com.example.demo.domain.playlist.dto.PlaylistDetailResponse;
import com.example.demo.domain.playlist.dto.PlaylistResponse;
import com.example.demo.domain.playlist.dto.PlaylistSortOption;
import java.util.List;

public interface PlaylistService {

    List<PlaylistResponse> getMyPlaylistsSorted(String userId, PlaylistSortOption sortOption);

    PlaylistDetailResponse getPlaylistDetail(String id, Long playlistId);
}
