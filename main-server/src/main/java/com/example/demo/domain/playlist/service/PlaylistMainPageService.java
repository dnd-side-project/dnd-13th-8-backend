package com.example.demo.domain.playlist.service;

import com.example.demo.domain.playlist.dto.GenreDto;
import com.example.demo.domain.playlist.dto.playlistdto.PlaylistDetailResponse;
import com.example.demo.domain.playlist.dto.search.PlaylistSearchDto;
import com.example.demo.domain.recommendation.dto.PlaylistRecommendationResponse;
import com.example.demo.domain.recommendation.dto.RecommendedPlaylistCard;
import java.util.List;

public interface PlaylistMainPageService {

    PlaylistDetailResponse getPlaylistDetail(Long playlistId, String userId);

    PlaylistRecommendationResponse getRecommendations(String userId);

    List<PlaylistSearchDto> recommendFromLikedPlaylists(String myUserId);

    List<GenreDto> recommendGenres(String userId);


}
