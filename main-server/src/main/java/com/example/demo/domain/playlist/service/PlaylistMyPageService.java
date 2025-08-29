package com.example.demo.domain.playlist.service;

import com.example.demo.domain.cd.dto.request.CdItemRequest;
import com.example.demo.domain.follow.dto.FollowPlaylistsResponse;
import com.example.demo.domain.playlist.dto.playlistdto.PlaylistCreateRequest;
import com.example.demo.domain.playlist.dto.playlistdto.PlaylistDetailResponse;
import com.example.demo.domain.playlist.dto.playlistdto.PlaylistResponse;
import com.example.demo.domain.playlist.dto.PlaylistSortOption;
import com.example.demo.domain.playlist.dto.playlistdto.PlaylistWithSongsResponse;
import java.util.List;

public interface PlaylistMyPageService {

    PlaylistWithSongsResponse saveFinalPlaylistWithSongsAndCd(String usersId, PlaylistCreateRequest request, List<CdItemRequest> cdItemRequestList);

    List<PlaylistResponse> getMyPlaylistsSorted(String userId, PlaylistSortOption sortOption);

    PlaylistDetailResponse getPlaylistDetail(String id, Long playlistId);

    void deletePlaylist(String userId, Long playlistId);

    String sharePlaylist(String userId);

    void updateRepresentative(String userId, Long playlistId);

    FollowPlaylistsResponse getFolloweePlaylists(String userId, PlaylistSortOption sort);

    List<PlaylistDetailResponse> getPlaylistsByCreatorId(String creatorId);
}
