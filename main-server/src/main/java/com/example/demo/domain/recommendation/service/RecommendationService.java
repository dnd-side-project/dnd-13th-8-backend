package com.example.demo.domain.recommendation.service;

import com.example.demo.domain.recommendation.dto.PlaylistCardResponse;
import com.example.demo.domain.recommendation.dto.RecommendedGenreResponse;

import java.util.List;

public interface RecommendationService {

    List<PlaylistCardResponse> getRecommendations(String userId);

    List<PlaylistCardResponse> recommendFromLikedPlaylists(String myUserId);

    List<RecommendedGenreResponse> recommendGenres(String userId);
}
