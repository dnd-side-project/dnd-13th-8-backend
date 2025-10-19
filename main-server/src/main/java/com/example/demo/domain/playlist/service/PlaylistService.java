package com.example.demo.domain.playlist.service;

import com.example.demo.domain.cd.dto.request.CdItemRequest;
import com.example.demo.domain.playlist.dto.playlistdto.MainPlaylistDetailResponse;
import com.example.demo.domain.playlist.dto.playlistdto.PlaylistCreateRequest;
import com.example.demo.domain.playlist.dto.playlistdto.PlaylistWithSongsResponse;

import java.util.List;

public interface PlaylistService {

    MainPlaylistDetailResponse playPlaylist(Long playlistId, String userId);

    MainPlaylistDetailResponse getPlaylistDetail(Long playlistId, String userId);

    PlaylistWithSongsResponse saveFinalPlaylistWithSongsAndCd(String usersId, PlaylistCreateRequest request, List<CdItemRequest> cdItemRequestList);

    PlaylistWithSongsResponse editFinalPlaylistWithSongsAndCd(String usersId, Long playlistId, PlaylistCreateRequest request,
                                                              List<CdItemRequest> cdItemRequestList);

    void deletePlaylist(String userId, Long playlistId);

}
