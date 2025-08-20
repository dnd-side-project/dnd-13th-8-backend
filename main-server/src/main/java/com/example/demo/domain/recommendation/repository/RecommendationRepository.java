package com.example.demo.domain.recommendation.repository;

import com.example.demo.domain.recommendation.entity.UserPlaylistHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendationRepository extends JpaRepository<UserPlaylistHistory, Long>, RecommendationRepositoryCustom {
}
