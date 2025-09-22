package com.example.demo.domain.playlist.controller;

import com.example.demo.domain.follow.dto.response.FollowedPlaylistsResponse;
import com.example.demo.domain.playlist.dto.*;
import com.example.demo.domain.playlist.dto.playlistdto.PlaylistDetailResponse;
import com.example.demo.domain.playlist.dto.playlistdto.PlaylistResponse;
import com.example.demo.domain.playlist.service.PlaylistMyPageService;
import com.example.demo.domain.representative.entity.RepresentativePlaylist;
import com.example.demo.domain.representative.service.RepresentativePlaylistService;
import com.example.demo.domain.user.entity.Users;
import com.example.demo.domain.user.service.UsersService;
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
    private final RepresentativePlaylistService representativePlaylistService;
    private final UsersService usersService;

    @GetMapping("/me/representative")
    @Operation(summary = "내 대표 플레이리스트 조회")
    public ResponseEntity<PlaylistDetailResponse> getMyRepresentativePlaylist(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        PlaylistDetailResponse dto = representativePlaylistService.getMyRepresentativePlaylist(userDetails.getId());
        return ResponseEntity.ok(dto);
    }

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
            description = "플레이리스트 상세 및 곡 목록, 좋아요/조회수 등 메타 정보를 반환합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "플레이리스트 상세",
            content = @Content(schema = @Schema(implementation = PlaylistDetailResponse.class))
    )
    @ApiResponse(responseCode = "404", description = "플레이리스트를 찾을 수 없음")
    @GetMapping("/me/{playlistId}")
    public ResponseEntity<PlaylistDetailResponse> getPlaylistDetail(
            @Parameter(description = "플레이리스트 ID", example = "123")
            @PathVariable Long playlistId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        PlaylistDetailResponse response = playlistMyPageService.getPlaylistDetail(user.getId(), playlistId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "공유된 링크로 플레이리스트 상세 조회",
            description = "공유된 링크로 플레이리스트 상세 및 곡 목록, 좋아요/조회수 등 메타 정보를 반환합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "플레이리스트 상세",
            content = @Content(schema = @Schema(implementation = PlaylistDetailResponse.class))
    )
    @ApiResponse(responseCode = "404", description = "플레이리스트를 찾을 수 없음")
    @GetMapping("/shared/{shareCode}")
    public ResponseEntity<PlaylistDetailResponse> getSharedPlaylistDetail(
            @Parameter(description = "플레이리스트 ID", example = "123")
            @PathVariable String shareCode
    ) {
        Users user = usersService.findUserByShareCode(shareCode);
        RepresentativePlaylist representativePlaylist = representativePlaylistService.findRepresentativePlaylistByUserId(user.getId());
        PlaylistDetailResponse response = playlistMyPageService.getPlaylistDetail(user.getId(), representativePlaylist.getPlaylist().getId());
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "내 플레이리스트 공유 코드 생성",
            description = "플레이리스트 공유를 위한 공유 코드를 발급합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "공유 코드",
            content = @Content(schema = @Schema(implementation = String.class), examples = {
                    @ExampleObject(name = "shareCode", value = "\"PL-ABCD-1234\"")
            })
    )
    @PostMapping("/me/share")
    public ResponseEntity<String> sharePlaylist(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        String shareCode = playlistMyPageService.sharePlaylist(user.getId());
        return ResponseEntity.ok(shareCode);
    }

    @Operation(
            summary = "대표 플레이리스트 설정/변경",
            description = "해당 플레이리스트를 대표로 설정합니다. 기존 대표는 자동 해제됩니다."
    )
    @ApiResponse(responseCode = "204", description = "대표 설정 완료")
    @PatchMapping("/me/{playlistId}/representative")
    public ResponseEntity<Void> updateRepresentative(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails user,
            @Parameter(description = "플레이리스트 ID", example = "123")
            @PathVariable Long playlistId
    ) {
        playlistMyPageService.updateRepresentative(user.getId(), playlistId);
        return ResponseEntity.noContent().build();
    }
}
