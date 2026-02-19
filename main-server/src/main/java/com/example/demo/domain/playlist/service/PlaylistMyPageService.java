package com.example.demo.domain.playlist.service;

import com.example.demo.domain.follow.dto.response.FollowedPlaylistsResponse;
import com.example.demo.domain.playlist.dto.common.PlaylistDetailWithCreatorResponse;
import com.example.demo.domain.playlist.dto.common.PlaylistDetailResponse;
import com.example.demo.domain.playlist.dto.common.PlaylistCoverResponse;
import com.example.demo.domain.playlist.dto.common.PlaylistSortOption;
import java.util.List;

public interface PlaylistMyPageService {

    List<PlaylistCoverResponse> getMyPlaylistsSorted(String userId, PlaylistSortOption sortOption);

    List<PlaylistCoverResponse> getLikedPlaylistsSorted(String userId, PlaylistSortOption sortOption);

    PlaylistDetailWithCreatorResponse getMyPlaylistDetail(String id, Long playlistId);

    void updateIsPublic(String userId, Long playlistId);

    FollowedPlaylistsResponse getFolloweePlaylists(String userId, PlaylistSortOption sort);

    List<PlaylistDetailResponse> getPlaylistsByCreatorId(String creatorId);


}
