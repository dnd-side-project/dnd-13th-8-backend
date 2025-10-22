package com.example.demo.domain.playlist.controller;

import com.example.demo.domain.follow.dto.response.FollowedPlaylistsResponse;
import com.example.demo.domain.playlist.dto.*;
import com.example.demo.domain.playlist.dto.playlistdto.MainPlaylistDetailResponse;
import com.example.demo.domain.playlist.dto.playlistdto.PlaylistDetailResponse;
import com.example.demo.domain.playlist.dto.playlistdto.PlaylistResponse;
import com.example.demo.domain.playlist.service.PlaylistMyPageService;
import com.example.demo.global.security.filter.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/main/playlist/mypage")
@RequiredArgsConstructor
@Tag(name = "Playlist - MyPage", description = "마이페이지 플레이리스트 관리 API")
@SecurityRequirement(name = "bearerAuth")
public class PlaylistMyPageController {

    private final PlaylistMyPageService playlistMyPageService;

    @Operation(
            summary = "내 플레이리스트 목록 조회",
            description = "정렬 옵션(POPULAR/RECENT)에 맞춰 내 플레이리스트를 조회합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "플레이리스트 목록",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = PlaylistResponse.class)))
    )
    @GetMapping("/me")
    public ResponseEntity<List<PlaylistResponse>> getMyPlaylists(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails user,
            @Parameter(description = "정렬 옵션 (기본 POPULAR)", example = "POPULAR")
            @RequestParam(defaultValue = "POPULAR") PlaylistSortOption sort
    ) {
        List<PlaylistResponse> myPlaylistsSorted = playlistMyPageService.getMyPlaylistsSorted(user.getId(), sort);
        return ResponseEntity.ok(myPlaylistsSorted);
    }

    @Operation(
            summary = "좋아요한 플레이리스트 목록 조회",
            description = "정렬 옵션(POPULAR/RECENT)에 맞춰 좋아요한 플레이리스트를 조회합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "플레이리스트 목록",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = PlaylistResponse.class)))
    )
    @GetMapping("/me/likes")
    public ResponseEntity<List<PlaylistResponse>> getLikedPlaylists(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails user,
            @Parameter(description = "정렬 옵션 (기본 POPULAR)", example = "POPULAR")
            @RequestParam(defaultValue = "POPULAR") PlaylistSortOption sort
    ) {
        List<PlaylistResponse> LikedPlaylistsSorted = playlistMyPageService.getLikedPlaylistsSorted(user.getId(), sort);
        return ResponseEntity.ok(LikedPlaylistsSorted);
    }

    @Operation(
            summary = "팔로우한 유저들의 플레이리스트 조회",
            description = "사용자가 팔로우한 유저들의 플레이리스트를 정렬 조건(POPULAR / RECENT)에 따라 조회합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "팔로우한 유저들의 플레이리스트 응답",
            content = @Content(schema = @Schema(implementation = FollowedPlaylistsResponse.class))
    )
    @GetMapping("/me/follows")
    public ResponseEntity<FollowedPlaylistsResponse> getFolloweePlaylists(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails user,

            @Parameter(description = "정렬 옵션 (RECENT or POPULAR)", example = "RECENT")
            @RequestParam(defaultValue = "RECENT") PlaylistSortOption sort
    ) {
        return ResponseEntity.ok(playlistMyPageService.getFolloweePlaylists(user.getId(), sort));
    }


    @Operation(
            summary = "특정 제작자의 플레이리스트 목록 조회",
            description = "제작자(creatorId)의 플레이리스트 목록을 반환합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "플레이리스트 목록",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = PlaylistDetailResponse.class)))
    )
    @GetMapping
    public ResponseEntity<List<PlaylistDetailResponse>> getPlaylistsByCreator(
            @Parameter(description = "제작자 ID", example = "user-1234")
            @RequestParam String creatorId
    ) {
        List<PlaylistDetailResponse> playlists = playlistMyPageService.getPlaylistsByCreatorId(creatorId);
        return ResponseEntity.ok(playlists);
    }


    @Operation(
            summary = "내 플레이리스트 상세 조회",
            description = "내 플레이리스트 상세 및 곡 목록, 좋아요/조회수 등 메타 정보를 반환합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "플레이리스트 상세",
            content = @Content(schema = @Schema(implementation = MainPlaylistDetailResponse.class))
    )
    @ApiResponse(responseCode = "404", description = "플레이리스트를 찾을 수 없음")
    @GetMapping("/me/{playlistId}")
    public ResponseEntity<MainPlaylistDetailResponse> getMyPlaylistDetail(
            @Parameter(description = "플레이리스트 ID", example = "123")
            @PathVariable Long playlistId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        MainPlaylistDetailResponse response = playlistMyPageService.getMyPlaylistDetail(user.getId(), playlistId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "플레이리스트 공개 설정/변경",
            description = "해당 플레이리스트를 공개/비공개로 설정합니다."
    )
    @ApiResponse(responseCode = "204", description = "공개 설정 완료")
    @PatchMapping("/me/{playlistId}/public")
    public ResponseEntity<Void> updatePlaylistPublicStatus(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails user,
            @Parameter(description = "플레이리스트 ID", example = "123")
            @PathVariable Long playlistId
    ) {
        playlistMyPageService.updateIsPublic(user.getId(), playlistId);
        return ResponseEntity.noContent().build();
    }
}
