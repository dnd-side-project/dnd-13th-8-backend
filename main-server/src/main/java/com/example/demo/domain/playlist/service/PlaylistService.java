package com.example.demo.domain.playlist.service;

import com.example.demo.domain.playlist.dto.EditFinalPlaylistRequest;
import com.example.demo.domain.playlist.dto.FinalPlaylistRequest;
import com.example.demo.domain.playlist.dto.PlaylistDraft;
import com.example.demo.domain.playlist.dto.playlistdto.MainPlaylistDetailResponse;
import com.example.demo.domain.playlist.dto.playlistdto.SavePlaylistRequest;
import com.example.demo.domain.playlist.dto.playlistdto.PlaylistWithSongsResponse;

public interface PlaylistService {

    MainPlaylistDetailResponse playPlaylist(Long playlistId, String userId);

    MainPlaylistDetailResponse getPlaylistDetail(Long playlistId, String userId);

    PlaylistWithSongsResponse saveFinalPlaylist(String usersId, String draftId);

    PlaylistWithSongsResponse editFinalPlaylist(String usersId, Long playlistId ,String draftId);

    String saveDraftPlaylist(PlaylistDraft playlistDraft);

    PlaylistWithSongsResponse saveFinalPlaylistWithSongsAndCd(String usersId, SavePlaylistRequest savePlaylistRequest,
                                                              FinalPlaylistRequest finalPlaylistRequest);

    PlaylistWithSongsResponse editFinalPlaylistWithSongsAndCd(String usersId, SavePlaylistRequest savePlaylistRequest,
                                                              EditFinalPlaylistRequest editFinalPlaylistRequest);

    void deletePlaylist(String userId, Long playlistId);

}
