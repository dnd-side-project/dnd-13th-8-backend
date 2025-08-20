package com.example.demo.domain.recommendation.controller;


import com.example.demo.domain.recommendation.dto.PlaylistRecommendationResponse;
import com.example.demo.domain.recommendation.service.RecommendationService;
import com.example.demo.global.security.filter.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/main/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    /**
     * 사용자 맞춤 추천 플레이리스트 조회
     * - 최근에 들은 장르 기반 추천 (3개)
     * - 좋아요 많은 추천 (3개)
     * - 최근 기록 없을 경우, 좋아요 기반으로 6개
     */
    @GetMapping
    public ResponseEntity<PlaylistRecommendationResponse> getRecommendations(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        PlaylistRecommendationResponse response = recommendationService.recommendPlaylists(user.getId());
        return ResponseEntity.ok(response);
    }
}

