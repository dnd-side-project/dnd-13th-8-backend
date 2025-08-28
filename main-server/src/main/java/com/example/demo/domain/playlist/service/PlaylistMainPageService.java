package com.example.demo.domain.playlist.service;

import com.example.demo.domain.playlist.dto.playlistdto.MainPlaylistDetailResponse;
import com.example.demo.domain.playlist.dto.playlistdto.PlaylistDetailResponse;
import com.example.demo.domain.recommendation.dto.PlaylistCardResponse;
import com.example.demo.domain.recommendation.dto.RecommendedGenreResponse;
import java.util.List;

public interface PlaylistMainPageService {

    MainPlaylistDetailResponse getPlaylistDetail(Long playlistId, String userId);

    List<PlaylistCardResponse> getRecommendations(String userId);

    List<PlaylistCardResponse> recommendFromLikedPlaylists(String myUserId);

    List<RecommendedGenreResponse> recommendGenres(String userId);


}
