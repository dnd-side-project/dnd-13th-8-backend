package com.example.demo.domain.recommendation.repository;

import com.example.demo.domain.recommendation.dto.RecommendedPlaylistResponseDto;
import java.util.List;

public interface RecommendationRepositoryCustom {

    List<RecommendedPlaylistResponseDto> findByUserRecentGenre(String userId, int limit);

    List<RecommendedPlaylistResponseDto> findByLikeCount(int limit);
}
