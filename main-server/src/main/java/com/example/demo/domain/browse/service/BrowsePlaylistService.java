package com.example.demo.domain.browse.service;

import com.example.demo.domain.browse.dto.BrowseResponse;

public interface BrowsePlaylistService {
    BrowseResponse getShuffledPlaylists(String userId, int cursor, int size);
}
