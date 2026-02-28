package com.example.demo.domain.recommendation.service;

import com.example.demo.domain.recommendation.dto.RecommendedPlaylistResponse;
import com.example.demo.domain.recommendation.dto.RecommendedGenreResponse;

import java.util.List;

public interface RecommendationService {

    List<RecommendedPlaylistResponse> getRecommendations(String userId);

    List<RecommendedPlaylistResponse> recommendFromLikedPlaylists(String myUserId);

    List<RecommendedGenreResponse> recommendGenres(String userId);

    List<RecommendedPlaylistResponse> getAdminRecommendation(int limit);

    List<RecommendedPlaylistResponse> getWeeklyTopRecommendation(int limit);
}
