package com.example.demo.domain.recommendation.service;

import com.example.demo.domain.recommendation.dto.GetTimeRecommendationResponse;
import com.example.demo.domain.recommendation.dto.RecommendedPlaylistResponse;
import com.example.demo.domain.recommendation.dto.RecommendedGenreResponse;
import com.example.demo.domain.recommendation.dto.RecommendedUserResponse;
import com.example.demo.domain.recommendation.entity.bundle.BundleTimeSlot;

import java.util.List;

public interface RecommendationService {

    List<RecommendedPlaylistResponse> getRecommendations(String userId);

    List<RecommendedPlaylistResponse> recommendFromLikedPlaylists(String myUserId);

    List<RecommendedGenreResponse> recommendGenres(String userId);

    List<RecommendedPlaylistResponse> getAdminRecommendation(int limit);

    List<RecommendedPlaylistResponse> getWeeklyTopRecommendation(int limit);

    List<RecommendedUserResponse> recommendTopFollowedUsers(String userId, int limit);

    List<GetTimeRecommendationResponse> getTimeRecommendation(BundleTimeSlot timeSlot, String userId);
}
