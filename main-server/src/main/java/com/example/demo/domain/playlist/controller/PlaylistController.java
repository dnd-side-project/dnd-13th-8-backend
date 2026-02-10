package com.example.demo.domain.playlist.controller;

import com.example.demo.domain.playlist.dto.EditFinalPlaylistRequest;
import com.example.demo.domain.playlist.dto.FinalPlaylistRequest;
import com.example.demo.domain.playlist.dto.PlaylistDraft;
import com.example.demo.domain.playlist.dto.playlistdto.MainPlaylistDetailResponse;
import com.example.demo.domain.playlist.dto.playlistdto.SavePlaylistRequest;
import com.example.demo.domain.playlist.dto.playlistdto.PlaylistWithSongsResponse;
import com.example.demo.domain.playlist.service.PlaylistService;
import com.example.demo.global.security.filter.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
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
            content = @Content(schema = @Schema(implementation = MainPlaylistDetailResponse.class))
    )
    @PostMapping("/{playlistId}/")
    public ResponseEntity<MainPlaylistDetailResponse> playPlaylist(
            @Parameter(description = "플레이리스트 ID", example = "101")
            @PathVariable Long playlistId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        MainPlaylistDetailResponse response = playlistService.playPlaylist(playlistId, user.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "플레이리스트 상세 정보 조회",
            description = "플레이리스트 상세 정보를 조회합니다"
    )
    @ApiResponse(
            responseCode = "200",
            description = "플레이리스트 상세 정보",
            content = @Content(schema = @Schema(implementation = MainPlaylistDetailResponse.class))
    )
    @GetMapping("/{playlistId}")
    public ResponseEntity<MainPlaylistDetailResponse> getPlaylistDetail(
            @Parameter(description = "플레이리스트 ID", example = "101")
            @PathVariable Long playlistId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        MainPlaylistDetailResponse response = playlistService.getPlaylistDetail(playlistId, user.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "임시 플레이리스트 저장(세션)",
            description = "플레이리스트 생성 전 단계에서 본문을 세션에 임시 저장합니다."
    )
    @ApiResponse(responseCode = "200", description = "임시 저장 완료")
    @PostMapping("/temp")
    public ResponseEntity<Void> saveTempPlaylist(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(schema = @Schema(implementation = SavePlaylistRequest.class))
            )
            @RequestBody @Valid SavePlaylistRequest request,
            HttpSession session
    ) {
        log.info("[/temp] SESSION ID = {}", session.getId());
        log.info("[/temp] Saving tempPlaylist = {}", request);

        session.setAttribute("tempPlaylist", request);
        return ResponseEntity.ok().build();
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
            content = @Content(schema = @Schema(implementation = PlaylistWithSongsResponse.class))
    )
    @ApiResponse(responseCode = "409", description = "임시 저장본 없음")
    @PostMapping("/v2/final/{draftId}")
    public ResponseEntity<PlaylistWithSongsResponse> saveDraftPlaylist(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable String draftId
    ) {
        PlaylistWithSongsResponse response = playlistService.saveFinalPlaylist(user.getId(), draftId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "플레이리스트 수정 저장",
            description = "세션에 저장된 임시본과 CD 요청을 사용하여 플레이리스트를 수정합니다"
    )
    @ApiResponse(
            responseCode = "200",
            description = "생성된 플레이리스트 상세",
            content = @Content(schema = @Schema(implementation = PlaylistWithSongsResponse.class))
    )
    @ApiResponse(responseCode = "409", description = "임시 저장본 없음")
    @PatchMapping("/v2/final/playlist/{playlistId}/draft/{draftId}")
    public ResponseEntity<PlaylistWithSongsResponse> editFinalPlaylist(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long playlistId, @PathVariable String draftId
    ) {
        PlaylistWithSongsResponse response = playlistService.editFinalPlaylist(user.getId(),
                playlistId, draftId);

        return ResponseEntity.ok(response);
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
        log.info("[/final] SESSION ID = {}", session.getId());

        SavePlaylistRequest request = (SavePlaylistRequest) session.getAttribute("tempPlaylist");
        if (request == null) {
            throw new IllegalStateException("세션에 임시 저장된 플레이리스트가 없습니다.");
        }

        PlaylistWithSongsResponse response = playlistService.saveFinalPlaylistWithSongsAndCd(user.getId(), request, finalPlaylistRequest);

        session.removeAttribute("tempPlaylist");
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "플레이리스트 수정(세션 임시본 사용 + Cd 수정)",
            description = "세션에 저장된 임시본과 CD 요청을 사용하여 플레이리스트를 수정합니다"
    )
    @ApiResponse(
            responseCode = "200",
            description = "생성된 플레이리스트 상세",
            content = @Content(schema = @Schema(implementation = PlaylistWithSongsResponse.class))
    )
    @ApiResponse(responseCode = "409", description = "세션에 임시 저장본 없음")
    @PatchMapping("/final")
    public ResponseEntity<PlaylistWithSongsResponse> editPlaylist(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody EditFinalPlaylistRequest editFinalPlaylistRequest,
            HttpSession session
    ) {
        SavePlaylistRequest request = (SavePlaylistRequest) session.getAttribute("tempPlaylist");
        if (request == null) {
            throw new IllegalStateException("세션에 임시 저장된 플레이리스트가 없습니다.");
        }

        PlaylistWithSongsResponse response = playlistService.editFinalPlaylistWithSongsAndCd(user.getId(),
                request, editFinalPlaylistRequest);

        session.removeAttribute("tempPlaylist");
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

}
