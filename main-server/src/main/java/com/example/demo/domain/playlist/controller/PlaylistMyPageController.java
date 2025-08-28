package com.example.demo.domain.playlist.controller;

import com.example.demo.domain.follow.dto.FollowPlaylistsResponse;
import com.example.demo.domain.playlist.dto.*;
import com.example.demo.domain.playlist.dto.playlistdto.PlaylistCreateRequest;
import com.example.demo.domain.playlist.dto.playlistdto.PlaylistDetailResponse;
import com.example.demo.domain.playlist.dto.playlistdto.PlaylistResponse;
import com.example.demo.domain.playlist.dto.playlistdto.PlaylistWithSongsResponse;
import com.example.demo.domain.playlist.service.PlaylistMyPageService;
import com.example.demo.domain.representative.entity.RepresentativePlaylist;
import com.example.demo.domain.representative.service.RepresentativePlaylistService;
import com.example.demo.domain.user.dto.UpdateProfileRequest;
import com.example.demo.domain.user.dto.UpdateProfileResponse;
import com.example.demo.domain.user.entity.Users;
import com.example.demo.domain.user.service.UsersService;
import com.example.demo.global.security.filter.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/main/mypage/playlists")
@RequiredArgsConstructor
@Tag(name = "MyPage - Playlists", description = "마이페이지 내 플레이리스트 관리 API")
@SecurityRequirement(name = "bearerAuth")
public class PlaylistMyPageController {

    private final PlaylistMyPageService playlistMyPageService;
    private final RepresentativePlaylistService representativePlaylistService;
    private final UsersService usersService;

    @Operation(
            summary = "임시 플레이리스트 저장(세션)",
            description = "플레이리스트 생성 전 단계에서 본문을 세션에 임시 저장합니다."
    )
    @ApiResponse(responseCode = "200", description = "임시 저장 완료")
    @PostMapping("/temp")
    public ResponseEntity<Void> saveTempPlaylist(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(schema = @Schema(implementation = PlaylistCreateRequest.class))
            )
            @RequestBody @Valid PlaylistCreateRequest request,
            HttpSession session
    ) {
        session.setAttribute("tempPlaylist", request);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "플레이리스트 생성(세션 임시본 사용 + Cd 요청)",
            description = "세션에 저장된 임시본과 CD 요청을 사용하여 실제 플레이리스트를 생성합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "생성된 플레이리스트 상세",
            content = @Content(schema = @Schema(implementation = PlaylistWithSongsResponse.class))
    )
    @ApiResponse(responseCode = "409", description = "세션에 임시 저장본 없음")
    @PostMapping("/final")
    public ResponseEntity<PlaylistWithSongsResponse> savePlaylist(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody FinalPlaylistRequest finalPlaylistRequest,
            HttpSession session
    ) {
        PlaylistCreateRequest request = (PlaylistCreateRequest) session.getAttribute("tempPlaylist");
        if (request == null) {
            throw new IllegalStateException("세션에 임시 저장된 플레이리스트가 없습니다.");
        }

        PlaylistWithSongsResponse response = playlistMyPageService.saveFinalPlaylistWithSongsAndCd(user.getId(), request, finalPlaylistRequest.saveCdRequestDto().cdItems());

        session.removeAttribute("tempPlaylist");
        return ResponseEntity.ok(response);
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
            content = @Content(schema = @Schema(implementation = FollowPlaylistsResponse.class))
    )
    @GetMapping("/follows")
    public ResponseEntity<FollowPlaylistsResponse> getFolloweePlaylists(
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
            summary = "내 플레이리스트 삭제",
            description = "플레이리스트를 삭제합니다."
    )
    @ApiResponse(responseCode = "204", description = "삭제 완료")
    @ApiResponse(responseCode = "403", description = "권한 없음")
    @DeleteMapping("/me/{playlistId}")
    public ResponseEntity<Void> deletePlaylist(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails user,
            @Parameter(description = "플레이리스트 ID", example = "123")
            @PathVariable Long playlistId
    ) {
        playlistMyPageService.deletePlaylist(user.getId(), playlistId);
        return ResponseEntity.noContent().build();
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

    @PatchMapping("/me")
    public ResponseEntity<UpdateProfileResponse> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @ModelAttribute UpdateProfileRequest request // MultipartFile + String 같이 받기 위해 @ModelAttribute
    ) throws IOException {
        UpdateProfileResponse response = usersService.updateProfile(userDetails.getId(), request);
        return ResponseEntity.ok(response);
    }
}
