package com.example.demo.domain.playlist.controller;

import com.example.demo.domain.playlist.dto.PlaylistDetailResponse;
import com.example.demo.domain.playlist.service.PlaylistMainPageService;

import com.example.demo.domain.recommendation.dto.PlaylistRecommendationResponse;
import com.example.demo.global.security.filter.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/playlists")
@RequiredArgsConstructor
public class PlaylistsMainPageController {

    private final PlaylistMainPageService playlistMainPageService;

    /**
     * 플레이리스트 상세 조회 + 재생 기록 저장
     */
    @GetMapping("/{playlistId}")
    public ResponseEntity<PlaylistDetailResponse> getPlaylistDetail(
            @PathVariable Long playlistId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        PlaylistDetailResponse response = playlistMainPageService.getPlaylistDetail(playlistId, user.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * 메인 추천용 플레이리스트 목록 조회
     */
    @GetMapping("/recommendations")
    public ResponseEntity<PlaylistRecommendationResponse> getRecommendations(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        PlaylistRecommendationResponse response = playlistMainPageService.getRecommendations(user.getId());
        return ResponseEntity.ok(response);
    }
}
