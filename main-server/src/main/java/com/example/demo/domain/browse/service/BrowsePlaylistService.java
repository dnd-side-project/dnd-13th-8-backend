package com.example.demo.domain.browse.service;

import com.example.demo.domain.browse.dto.BrowsePlaylistCursor;
import com.example.demo.domain.browse.dto.BrowsePlaylistDto;
import com.example.demo.domain.playlist.dto.playlistdto.CursorPageResponse;

public interface BrowsePlaylistService {

    CursorPageResponse<BrowsePlaylistDto, BrowsePlaylistCursor> getShuffledPlaylists(
            String userId, Integer cursorPosition, Long cursorCardId, int size);

    void confirmAndLogPlayback(String id, Long playlistId);
}
