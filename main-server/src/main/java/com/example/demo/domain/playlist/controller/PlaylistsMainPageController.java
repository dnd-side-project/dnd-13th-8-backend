package com.example.demo.domain.playlist.controller;

import com.example.demo.domain.playlist.dto.GenreDto;
import com.example.demo.domain.playlist.dto.PlaylistDetailResponse;
import com.example.demo.domain.playlist.service.PlaylistMainPageService;
import com.example.demo.domain.recommendation.dto.PlaylistRecommendationResponse;
import com.example.demo.domain.recommendation.dto.RecommendedGenresResponse;
import com.example.demo.domain.recommendation.dto.RecommendedPlaylistCard;
import com.example.demo.domain.recommendation.dto.RecommendedPlaylistsWithSongsResponse;
import com.example.demo.global.security.filter.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/main/playlists")
@RequiredArgsConstructor
@Tag(name = "MainPage - Playlists", description = "메인 페이지 플레이리스트/추천 API")
@SecurityRequirement(name = "bearerAuth")
public class PlaylistsMainPageController {

    private final PlaylistMainPageService playlistMainPageService;


    @Operation(
            summary = "최근 들은 장르 기반 추천",
            description = "최근 사용자가 들은 장르를 기반으로 플레이리스트를 추천합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "추천 플레이리스트 목록",
            content = @Content(schema = @Schema(implementation = PlaylistRecommendationResponse.class))
    )
    @GetMapping("/recommendations/playlist")
    public ResponseEntity<PlaylistRecommendationResponse> getRecommendations(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        PlaylistRecommendationResponse response = playlistMainPageService.getRecommendations(user.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "좋아요 기반 제작자 추천",
            description = "내가 좋아요한 플레이리스트의 제작자를 기반으로 새로운 플레이리스트를 추천합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "추천된 플레이리스트 카드 목록",
            content = @Content(schema = @Schema(implementation = RecommendedPlaylistsWithSongsResponse.class))
    )
    @GetMapping("/recommendations/friend")
    public ResponseEntity<RecommendedPlaylistsWithSongsResponse> getOwnerBasedRecommendations(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        List<RecommendedPlaylistCard> recommended = playlistMainPageService.recommendFromLikedPlaylists(user.getId());
        return ResponseEntity.ok(new RecommendedPlaylistsWithSongsResponse(recommended));
    }

    @Operation(
            summary = "추천 장르 조회",
            description = "어제 인기 있는 장르 + 내 선호 장르를 종합하여 추천 장르를 반환합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "추천 장르 목록",
            content = @Content(schema = @Schema(implementation = RecommendedGenresResponse.class))
    )
    @GetMapping("/recommendations/genre")
    public ResponseEntity<RecommendedGenresResponse> getRecommendedGenre(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        List<GenreDto> genre = playlistMainPageService.recommendGenres(user.getId());
        return ResponseEntity.ok(new RecommendedGenresResponse(genre));
    }
}
