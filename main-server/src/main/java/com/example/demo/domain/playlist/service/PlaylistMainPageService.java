package com.example.demo.domain.playlist.service;

import com.example.demo.domain.playlist.dto.GenreDto;
import com.example.demo.domain.playlist.dto.PlaylistDetailResponse;
import com.example.demo.domain.playlist.dto.PlaylistGenre;
import com.example.demo.domain.playlist.dto.PlaylistLikeResponse;
import com.example.demo.domain.recommendation.dto.PlaylistRecommendationDto;
import com.example.demo.domain.recommendation.dto.PlaylistRecommendationResponse;
import com.example.demo.domain.recommendation.dto.RecommendedPlaylistCard;
import java.util.List;

public interface PlaylistMainPageService {

    PlaylistDetailResponse getPlaylistDetail(Long playlistId, String userId);

    PlaylistRecommendationResponse getRecommendations(String userId);

    List<RecommendedPlaylistCard> recommendFromLikedPlaylists(String myUserId);

    List<GenreDto> recommendGenres(String userId);

    PlaylistLikeResponse toggleLike(String userId, Long playlistId);

}
