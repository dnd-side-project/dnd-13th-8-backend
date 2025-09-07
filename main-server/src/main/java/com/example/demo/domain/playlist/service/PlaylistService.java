package com.example.demo.domain.playlist.service;

import com.example.demo.domain.playlist.dto.playlistdto.MainPlaylistDetailResponse;

public interface PlaylistService {

    MainPlaylistDetailResponse getPlaylistDetail(Long playlistId, String userId);


}
