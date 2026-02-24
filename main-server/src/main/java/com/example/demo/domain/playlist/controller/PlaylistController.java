package com.example.demo.domain.playlist.controller;

import com.example.demo.domain.playlist.dto.save.PlaylistDraft;
import com.example.demo.domain.playlist.dto.common.PlaylistDetailWithCreatorResponse;
import com.example.demo.domain.playlist.dto.save.SavePlaylistResponse;
import com.example.demo.domain.playlist.service.PlaylistService;
import com.example.demo.global.security.filter.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/main/playlist")
@Tag(name = "Playlist - CRUD", description = "플레이리스트 CRUD API")
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistService playlistService;

    @Operation(
            summary = "플레이리스트 재생",
            description = "플레이리스트 상세 정보를 조회하면서 동시에 재생 기록을 저장합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "플레이리스트 상세 정보",
            content = @Content(schema = @Schema(implementation = PlaylistDetailWithCreatorResponse.class))
    )
    @PostMapping("/{playlistId}/")
    public ResponseEntity<PlaylistDetailWithCreatorResponse> playPlaylist(
            @Parameter(description = "플레이리스트 ID", example = "101")
            @PathVariable Long playlistId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        PlaylistDetailWithCreatorResponse response = playlistService.playPlaylist(playlistId, user.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "플레이리스트 상세 정보 조회",
            description = "플레이리스트 상세 정보를 조회합니다"
    )
    @ApiResponse(
            responseCode = "200",
            description = "플레이리스트 상세 정보",
            content = @Content(schema = @Schema(implementation = PlaylistDetailWithCreatorResponse.class))
    )
    @GetMapping("/{playlistId}")
    public ResponseEntity<PlaylistDetailWithCreatorResponse> getPlaylistDetail(
            @Parameter(description = "플레이리스트 ID", example = "101")
            @PathVariable Long playlistId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        PlaylistDetailWithCreatorResponse response = playlistService.getPlaylistDetail(playlistId, user.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "플레이리스트 임시 저장",
            description = "플레이리스트를 임시 저장합니다"
    )
    @ApiResponse(responseCode = "200", description = "임시 저장 완료")
    @PostMapping("/v2/temp")
    public ResponseEntity<String> createDraftPlaylist(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(schema = @Schema(implementation = PlaylistDraft.class))
            )
            @RequestBody @Valid PlaylistDraft playlistDraft
    ) {
        String draftId = playlistService.saveDraftPlaylist(playlistDraft);
        return ResponseEntity.ok(draftId);
    }

    @Operation(
            summary = "플레이리스트 저장",
            description = "플레이리스트를 최종 저장합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "생성된 플레이리스트 상세",
            content = @Content(schema = @Schema(implementation = SavePlaylistResponse.class))
    )
    @ApiResponse(responseCode = "409", description = "임시 저장본 없음")
    @PostMapping("/v2/final/{draftId}")
    public ResponseEntity<SavePlaylistResponse> saveDraftPlaylist(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable String draftId
    ) {
        SavePlaylistResponse response = playlistService.saveFinalPlaylist(user.getId(), draftId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "플레이리스트 수정 저장",
            description = "세션에 저장된 임시본과 CD 요청을 사용하여 플레이리스트를 수정합니다"
    )
    @ApiResponse(
            responseCode = "200",
            description = "생성된 플레이리스트 상세",
            content = @Content(schema = @Schema(implementation = SavePlaylistResponse.class))
    )
    @ApiResponse(responseCode = "409", description = "임시 저장본 없음")
    @PatchMapping("/v2/final/playlist/{playlistId}/draft/{draftId}")
    public ResponseEntity<SavePlaylistResponse> editFinalPlaylist(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long playlistId, @PathVariable String draftId
    ) {
        SavePlaylistResponse response = playlistService.editFinalPlaylist(user.getId(),
                playlistId, draftId);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "내 플레이리스트 삭제",
            description = "플레이리스트를 삭제합니다."
    )
    @ApiResponse(responseCode = "204", description = "삭제 완료")
    @ApiResponse(responseCode = "403", description = "권한 없음")
    @DeleteMapping("/{playlistId}")
    public ResponseEntity<Void> deletePlaylist(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails user,
            @Parameter(description = "플레이리스트 ID", example = "123")
            @PathVariable Long playlistId
    ) {
        playlistService.deletePlaylist(user.getId(), playlistId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "플레이리스트 공개 설정/변경",
            description = "해당 플레이리스트를 공개/비공개로 설정합니다."
    )
    @ApiResponse(responseCode = "204", description = "공개 설정 완료")
    @PatchMapping("/{playlistId}/public")
    public ResponseEntity<Void> updatePlaylistPublicStatus(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails user,
            @Parameter(description = "플레이리스트 ID", example = "123")
            @PathVariable Long playlistId
    ) {
        playlistService.updateIsPublic(user.getId(), playlistId);
        return ResponseEntity.noContent().build();
    }

}
