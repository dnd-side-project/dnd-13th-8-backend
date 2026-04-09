package com.example.demo.domain.playlist.service.common;

import com.example.demo.domain.cd.dto.request.SaveCdRequest;
import com.example.demo.domain.playlist.dto.save.PlaylistDraft;
import com.example.demo.domain.playlist.dto.save.SavePlaylistRequest;
import com.example.demo.domain.playlist.dto.save.SavePlaylistResponse;
import com.example.demo.domain.playlist.entity.Playlist;

public interface PlaylistSaveService {
    String createDraft(SavePlaylistRequest savePlaylistRequest, SaveCdRequest saveCdRequest);

    PlaylistDraft loadDraft(String draftId);

    void deleteDraft(String draftId);

    SavePlaylistResponse savePlaylistWithSongs(String usersId, SavePlaylistRequest request);

    SavePlaylistResponse editPlaylistWithSongs(String usersId, Long playlistId, SavePlaylistRequest request);
}
