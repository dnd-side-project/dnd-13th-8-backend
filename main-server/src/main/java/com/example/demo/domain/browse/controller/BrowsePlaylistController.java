package com.example.demo.domain.browse.controller;

import com.example.demo.domain.browse.dto.PlaylistViewCountDto;
import com.example.demo.domain.browse.service.BrowsePlaylistService;
import com.example.demo.domain.browse.service.BrowseViewCountService;
import com.example.demo.domain.playlist.dto.playlistdto.CursorPageResponse;
import com.example.demo.global.security.filter.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Playlist - Browse", description = "둘러보기 페이지 유틸 API")
@RequestMapping("/main/playlist/browse")
public class BrowsePlaylistController {

    private final BrowsePlaylistService browsePlaylistService;
    private final BrowseViewCountService browseViewCountService;

    @GetMapping
    @Operation(
            summary = "둘러보기 알고리즘",
            description = "둘러보기 알고리즘을 통해 선별한 플레이리스트 ID 목록을 커서 방식으로 가져옵니다"
    )
    public ResponseEntity<CursorPageResponse<Long, Long>> browsePlaylists(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails user,

            @Parameter(description = "커서 id(=마지막 playlistId). 이 ID 이후부터 조회", example = "100")
            @RequestParam(required = false) Long cursorId,

            @Parameter(description = "한 페이지 개수(기본 20)", example = "10")
            @RequestParam(defaultValue = "20") int size
    ) {
        var resp = browsePlaylistService.getShuffledPlaylistIds(user.getId(), cursorId, size);
        return ResponseEntity.ok(resp);
    }



    @PostMapping("/start")
    @Operation(
            summary = "하트비트 시작 (재생 시작)",
            description = "사용자가 재생 버튼을 누른 시점에 호출됩니다. 실제 조회수는 증가하지 않으며, 타이머 기준점을 설정하는 용도입니다."
    )
    public void startHeartbeat(
            @RequestParam Long playlistId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        browseViewCountService.logHeartbeatStart(userDetails.getId(), playlistId);
    }

    @PostMapping("/confirm")
    @Operation(
            summary = "하트비트 확정 (10초 이상 재생)",
            description = """
            사용자가 15초 이상 곡을 재생한 경우 호출됩니다.
            Redis에 중복 확인 후, 조회수가 1 증가하며 하루에 한 번만 카운트됩니다.
        """
    )
    public void confirmHeartbeat(
            @RequestParam Long playlistId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        browsePlaylistService.confirmAndLogPlayback(userDetails.getId(), playlistId);
    }


    @GetMapping("/view-counts/{playlistId}")
    @Operation(
            summary = "플레이리스트 조회수 단건 조회",
            description = "지정한 플레이리스트 ID의 현재 조회수를 반환합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "단일 플레이리스트 조회수 정보",
            content = @Content(schema = @Schema(implementation = PlaylistViewCountDto.class))
    )
    public PlaylistViewCountDto getViewCount(
            @PathVariable
            @Parameter(description = "플레이리스트 ID", example = "101")
            Long playlistId
    ) {
        return browseViewCountService.getViewCount(playlistId);
    }
}
