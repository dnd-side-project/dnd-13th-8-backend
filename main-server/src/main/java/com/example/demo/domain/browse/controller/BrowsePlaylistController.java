package com.example.demo.domain.browse.controller;

import com.example.demo.domain.browse.dto.BrowsePlaylistCursor;
import com.example.demo.domain.browse.dto.BrowsePlaylistDto;
import com.example.demo.domain.browse.dto.BrowseResponse;
import com.example.demo.domain.browse.service.BrowsePlaylistService;
import com.example.demo.domain.browse.service.BrowseViewCountService;
import com.example.demo.domain.follow.service.PlaylistFollowService;
import com.example.demo.domain.playlist.dto.playlistdto.CursorPageResponse;
import com.example.demo.global.security.filter.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/main/browse/playlists")
public class BrowsePlaylistController {

    private final BrowsePlaylistService browsePlaylistService;
    private final PlaylistFollowService playlistFollowService;
    private final BrowseViewCountService browseViewCountService;

    @Operation(
            summary = "셔플된 플레이리스트 목록 조회 (둘러보기)",
            description = """
        사용자의 Redis에 캐싱된 셔플된 둘러보기(BrowsePlaylist) 목록을 커서 기반으로 조회합니다. 
        각 유저는 매일 새벽 3시에 셔플된 position 기반의 카드 목록을 가지며, position과 cardId를 함께 사용해 커서 페이징합니다.
        
        🔁 [Fallback 처리 안내]
        - 신규 가입자 등 캐시 데이터가 없는 경우: BrowsePlaylist 테이블의 ID 1~5번 중 하나를 무작위로 선택하여 반환합니다.
        - 이 경우 nextCursor는 null입니다.
    """
    )
    @GetMapping
    public ResponseEntity<CursorPageResponse<BrowsePlaylistDto, BrowsePlaylistCursor>> browsePlaylists(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails user,

            @Parameter(
                    description = "커서 position. 해당 position부터 이후 카드가 조회됩니다.",
                    example = "2"
            )
            @RequestParam(required = false) Integer cursorPosition,

            @Parameter(
                    description = "커서 cardId. 같은 position 내 카드 중 이 ID 이후의 카드부터 조회됩니다.",
                    example = "1"
            )
            @RequestParam(required = false) Long cursorCardId,

            @Parameter(
                    description = "한 페이지에서 가져올 카드 수 (기본값: 20)",
                    example = "20"
            )
            @RequestParam(defaultValue = "20") int size
    ) {
        log.info("들어왔으므" + user.getId());
        CursorPageResponse<BrowsePlaylistDto, BrowsePlaylistCursor> shuffledPlaylists = browsePlaylistService.getShuffledPlaylists(
                user.getId(), cursorPosition, cursorCardId, size
        );
        return ResponseEntity.ok(shuffledPlaylists);
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
            summary = "하트비트 확정 (15초 이상 재생)",
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

    @PostMapping("/view-counts")
    @Operation(
            summary = "여러 플레이리스트 조회수 일괄 조회",
            description = """
        
            프론트에서 실시간 폴링 용도로 사용합니다. 5초 간격으로 보내주세요
        """
    )
    public Map<Long, Long> getViewCounts(
            @RequestBody List<Long> playlistIds
    ) {
        return browseViewCountService.getViewCounts(playlistIds);
    }

    @PostMapping("/{playlistId}/follow")
    @Operation(
            summary = "플레이리스트 팔로우",
            description = "현재 로그인한 사용자가 지정된 playlistId를 팔로우합니다. 중복 팔로우는 무시됩니다."
    )
    public ResponseEntity<Void> followPlaylist(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails me,
            @PathVariable Long playlistId
    ) {
        playlistFollowService.follow(me.getId(), playlistId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{playlistId}/follow")
    @Operation(
            summary = "플레이리스트 팔로우 취소",
            description = "현재 로그인한 사용자가 지정된 playlistId를 언팔로우합니다. 팔로우 상태가 아니라면 아무 동작도 하지 않습니다."
    )
    public ResponseEntity<Void> unfollowPlaylist(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails me,
            @PathVariable Long playlistId
    ) {
        playlistFollowService.unfollow(me.getId(), playlistId);
        return ResponseEntity.ok().build();
    }
}
