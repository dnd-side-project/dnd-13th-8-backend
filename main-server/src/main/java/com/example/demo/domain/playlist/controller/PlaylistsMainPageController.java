package com.example.demo.domain.playlist.controller;

import com.example.demo.domain.playlist.dto.PlaylistDetailResponse;
import com.example.demo.domain.playlist.dto.PlaylistGenre;
import com.example.demo.domain.playlist.service.PlaylistMainPageService;

import com.example.demo.domain.recommendation.dto.PlaylistRecommendationDto;
import com.example.demo.domain.recommendation.dto.PlaylistRecommendationResponse;
import com.example.demo.domain.recommendation.dto.RecommendedGenreResponse;
import com.example.demo.domain.recommendation.dto.RecommendedPlaylistCard;
import com.example.demo.domain.recommendation.dto.RecommendedPlaylistsWithSongsResponse;
import com.example.demo.global.security.filter.CustomUserDetails;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/main/playlists")
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
     *  최근 들은 장르 기반 플레이리스트 추천
     */
    @GetMapping("/recommendations/genre")
    public ResponseEntity<PlaylistRecommendationResponse> getRecommendations(
            @AuthenticationPrincipal CustomUserDetails user) {
        PlaylistRecommendationResponse response = playlistMainPageService.getRecommendations(user.getId());
        return ResponseEntity.ok(response);
    }

    /**
     *  좋아요 누른 플레이리스트의 제작자 기반 추천
     */
    @GetMapping("/recommendations/friend")
    public ResponseEntity<RecommendedPlaylistsWithSongsResponse> getOwnerBasedRecommendations(
            @AuthenticationPrincipal CustomUserDetails user) {
        List<RecommendedPlaylistCard> recommended = playlistMainPageService.recommendFromLikedPlaylists(user.getId());
        return ResponseEntity.ok(new RecommendedPlaylistsWithSongsResponse(recommended));
    }

    @GetMapping("/recommendations/genre")
    public ResponseEntity<RecommendedGenreResponse> getRecommendedGenre(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        PlaylistGenre genre = playlistMainPageService.recommendGenreByPopularityOrUser(user.getId());
        return ResponseEntity.ok(new RecommendedGenreResponse(genre));
    }

}
