package com.example.demo.domain.recommendation.service;

import com.example.demo.domain.recommendation.dto.PlaylistRecommendationResponse;
import com.example.demo.domain.recommendation.dto.RecommendedPlaylistResponseDto;
import com.example.demo.domain.recommendation.repository.RecommendationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;

    public PlaylistRecommendationResponse recommendPlaylists(String userId) {
        // 1. 최근 장르 기반 추천 3개
        List<RecommendedPlaylistResponseDto> genreBased = recommendationRepository.findByUserRecentGenre(userId, 3);

        if (genreBased.isEmpty()) {
            // → 최근 기록 없으면 좋아요 기반 6개로 대체
            List<RecommendedPlaylistResponseDto> likeTop6 = recommendationRepository.findByLikeCount(6);
            return PlaylistRecommendationResponse.onlyLikes(likeTop6);
        }

        // 2. 추가로 좋아요 기반 3개
        List<RecommendedPlaylistResponseDto> likeTop3 = recommendationRepository.findByLikeCount(3);

        return PlaylistRecommendationResponse.of(genreBased, likeTop3);
    }
}
