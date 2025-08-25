package com.example.demo.domain.playlist.service;

import com.example.demo.domain.playlist.dto.playlistdto.PlaylistDetailResponse;
import com.example.demo.domain.recommendation.dto.PlaylistCardResponse;
import java.util.List;

public interface PlaylistMainPageService {

    PlaylistDetailResponse getPlaylistDetail(Long playlistId, String userId);

    List<PlaylistCardResponse> getRecommendations(String userId);

    List<PlaylistCardResponse> recommendFromLikedPlaylists(String myUserId);

    List<PlaylistDetailResponse> recommendGenres(String userId);


}
