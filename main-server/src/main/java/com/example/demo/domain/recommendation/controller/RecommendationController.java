package com.example.demo.domain.recommendation.controller;

import com.example.demo.domain.recommendation.dto.PlaylistCardResponse;
import com.example.demo.domain.recommendation.dto.RecommendedGenreResponse;
import com.example.demo.domain.recommendation.service.RecommendationService;
import com.example.demo.global.security.filter.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/main/recommendation")
@RequiredArgsConstructor
@Tag(name = "Recommendation", description = "추천 API")
@SecurityRequirement(name = "bearerAuth")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @Operation(
            summary = "최근 들은 장르 기반 추천",
            description = "최근 사용자가 들은 장르를 기반으로 플레이리스트를 추천합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "추천 플레이리스트 목록",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = PlaylistCardResponse.class)))
    )
    @GetMapping("/playlist")
    public ResponseEntity<List<PlaylistCardResponse>> getRecommendations(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        List<PlaylistCardResponse> response = recommendationService.getRecommendations(user.getId());
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
    @GetMapping("/follow")
    public ResponseEntity<List<PlaylistCardResponse>> recommendFromLikedPlaylists(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        List<PlaylistCardResponse> response = recommendationService.recommendFromLikedPlaylists(user.getId());
        return ResponseEntity.ok(response);
    }


    @Operation(summary = "추천 장르 기반 대표 플레이리스트 목록")
    @ApiResponse(responseCode = "200", description = "추천 플레이리스트 상세 목록",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = RecommendedGenreResponse.class))))
    @GetMapping("/genres")
    public ResponseEntity<List<RecommendedGenreResponse>> recommendGenres(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user
    ) {
        return ResponseEntity.ok(recommendationService.recommendGenres(user.getId()));
    }
}
