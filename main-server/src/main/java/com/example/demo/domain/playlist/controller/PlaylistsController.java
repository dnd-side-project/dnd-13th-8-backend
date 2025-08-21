package com.example.demo.domain.playlist.controller;

import com.example.demo.domain.playlist.dto.PlaylistDetailResponse;
import com.example.demo.domain.playlist.service.PlaylistMainPageService;
import com.example.demo.global.security.filter.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/main/playlists")
@Tag(name = "playlist-detail", description = "플레이리스트 상세보기")
@RequiredArgsConstructor
public class PlaylistsController {

    private final PlaylistMainPageService playlistMainPageService;

    @Operation(
            summary = "플레이리스트 상세 조회 + 재생 기록 저장",
            description = "플레이리스트 상세를 조회하면서 동시에 재생 기록을 저장합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "플레이리스트 상세 정보",
            content = @Content(schema = @Schema(implementation = PlaylistDetailResponse.class))
    )
    @GetMapping("/{playlistId}")
    public ResponseEntity<PlaylistDetailResponse> getPlaylistDetail(
            @Parameter(description = "플레이리스트 ID", example = "101")
            @PathVariable Long playlistId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        PlaylistDetailResponse response = playlistMainPageService.getPlaylistDetail(playlistId, user.getId());
        return ResponseEntity.ok(response);
    }

}
