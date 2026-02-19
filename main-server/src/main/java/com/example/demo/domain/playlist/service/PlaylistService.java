package com.example.demo.domain.playlist.service;

import com.example.demo.domain.playlist.dto.save.EditFinalPlaylistRequest;
import com.example.demo.domain.playlist.dto.save.FinalPlaylistRequest;
import com.example.demo.domain.playlist.dto.save.PlaylistDraft;
import com.example.demo.domain.playlist.dto.common.PlaylistDetailWithCreatorResponse;
import com.example.demo.domain.playlist.dto.save.SavePlaylistRequest;
import com.example.demo.domain.playlist.dto.save.SavePlaylistResponse;

public interface PlaylistService {

    PlaylistDetailWithCreatorResponse playPlaylist(Long playlistId, String userId);

    PlaylistDetailWithCreatorResponse getPlaylistDetail(Long playlistId, String userId);

    SavePlaylistResponse saveFinalPlaylist(String usersId, String draftId);

    SavePlaylistResponse editFinalPlaylist(String usersId, Long playlistId , String draftId);

    String saveDraftPlaylist(PlaylistDraft playlistDraft);

    SavePlaylistResponse saveFinalPlaylistWithSongsAndCd(String usersId, SavePlaylistRequest savePlaylistRequest,
                                                         FinalPlaylistRequest finalPlaylistRequest);

    SavePlaylistResponse editFinalPlaylistWithSongsAndCd(String usersId, SavePlaylistRequest savePlaylistRequest,
                                                         EditFinalPlaylistRequest editFinalPlaylistRequest);

    void deletePlaylist(String userId, Long playlistId);

}
