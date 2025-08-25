package com.example.demo.domain.playlist.controller;

import com.example.demo.domain.playlist.dto.playlistdto.PlaylistDetailResponse;
import com.example.demo.domain.playlist.service.PlaylistMainPageService;
import com.example.demo.domain.recommendation.dto.PlaylistCardResponse;
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
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = PlaylistCardResponse.class)))
    )
    @GetMapping("/recommendations/playlist")
    public ResponseEntity<List<PlaylistCardResponse>> getRecommendations(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        List<PlaylistCardResponse> response = playlistMainPageService.getRecommendations(user.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "팔로우 기반 추천",
            description = "사용자가 아직 팔로우하지 않은 최신 플레이리스트를 추천합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "추천된 플레이리스트 카드 목록",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = PlaylistCardResponse.class)))
    )
    @GetMapping("/recommendations/follow")
    public ResponseEntity<List<PlaylistCardResponse>> recommendFromLikedPlaylists(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        List<PlaylistCardResponse> response = playlistMainPageService.recommendFromLikedPlaylists(user.getId());
        return ResponseEntity.ok(response);
    }


    @Operation(summary = "추천 장르 기반 대표 플레이리스트 목록")
    @ApiResponse(responseCode = "200", description = "추천 플레이리스트 상세 목록",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = PlaylistDetailResponse.class))))
    @GetMapping("/recommendations/genres")
    public ResponseEntity<List<PlaylistDetailResponse>> recommendGenres(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user
    ) {
        return ResponseEntity.ok(playlistMainPageService.recommendGenres(user.getId()));
    }

}
