package com.example.demo.domain.playlist.service;

import com.example.demo.domain.playlist.dto.PlaylistDetailResponse;
import com.example.demo.domain.recommendation.dto.PlaylistRecommendationDto;
import com.example.demo.domain.recommendation.dto.PlaylistRecommendationResponse;
import com.example.demo.domain.recommendation.dto.RecommendedPlaylistCard;
import java.util.List;

public interface PlaylistMainPageService {

    /**
     * 플레이리스트 상세 정보 조회 + 재생 기록 저장
     */
    PlaylistDetailResponse getPlaylistDetail(Long playlistId, String userId);

    /**
     * 사용자 맞춤 추천 목록 조회
     */
    PlaylistRecommendationResponse getRecommendations(String userId);

    List<RecommendedPlaylistCard> recommendFromLikedPlaylists(String myUserId);
}
