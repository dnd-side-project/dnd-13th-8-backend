package com.example.demo.domain.playlist.service;

import com.example.demo.domain.follow.dto.response.FollowedPlaylistsResponse;
import com.example.demo.domain.playlist.dto.playlistdto.PlaylistDetailResponse;
import com.example.demo.domain.playlist.dto.playlistdto.PlaylistResponse;
import com.example.demo.domain.playlist.dto.PlaylistSortOption;
import java.util.List;

public interface PlaylistMyPageService {

    List<PlaylistResponse> getMyPlaylistsSorted(String userId, PlaylistSortOption sortOption);

    List<PlaylistResponse> getLikedPlaylistsSorted(String userId, PlaylistSortOption sortOption);

    PlaylistDetailResponse getPlaylistDetail(String id, Long playlistId);

    void updateIsPublic(String userId, Long playlistId);

    FollowedPlaylistsResponse getFolloweePlaylists(String userId, PlaylistSortOption sort);

    List<PlaylistDetailResponse> getPlaylistsByCreatorId(String creatorId);


}
