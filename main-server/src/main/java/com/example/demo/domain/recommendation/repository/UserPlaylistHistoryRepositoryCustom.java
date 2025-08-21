package com.example.demo.domain.recommendation.repository;

import com.example.demo.domain.recommendation.dto.RecommendedPlaylistResponse;
import java.util.List;

public interface UserPlaylistHistoryRepositoryCustom {

    List<RecommendedPlaylistResponse> findByUserRecentGenre(String userId, int limit);

    List<RecommendedPlaylistResponse> findByLikeCount(int limit);
}
