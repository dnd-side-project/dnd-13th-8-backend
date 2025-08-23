package com.example.demo.domain.browse.controller;

import com.example.demo.domain.browse.dto.BrowseResponse;
import com.example.demo.domain.browse.service.BrowsePlaylistService;
import com.example.demo.domain.follow.service.PlaylistFollowService;
import com.example.demo.global.security.filter.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/main/browse/playlists")
public class BrowsePlaylistController {

    private final BrowsePlaylistService browsePlaylistService;
    private final PlaylistFollowService playlistFollowService;

    @GetMapping
    @Operation(summary = "커서 기반 플레이리스트 조회 (둘러보기)", description = "셔플된 플레이리스트 목록을 커서 기반으로 15개씩 반환합니다.")
    public ResponseEntity<BrowseResponse> browsePlaylists(
            @AuthenticationPrincipal CustomUserDetails user,
            @Parameter(description = "다음 페이지를 위한 커서 (기본값: 0)", example = "0")
            @RequestParam(defaultValue = "0") int cursor,

            @Parameter(description = "가져올 개수 (기본값: 15)", example = "15")
            @RequestParam(defaultValue = "15") int size
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(browsePlaylistService.getShuffledPlaylists(user.getId(), cursor, size));
    }

    @PostMapping("/{playlistId}/follow")
    public ResponseEntity<Void> followPlaylist(@AuthenticationPrincipal CustomUserDetails me,
                                               @PathVariable Long playlistId) {
        playlistFollowService.follow(me.getId(), playlistId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{playlistId}/follow")
    public ResponseEntity<Void> unfollowPlaylist(@AuthenticationPrincipal CustomUserDetails me,
                                                 @PathVariable Long playlistId) {
        playlistFollowService.unfollow(me.getId(), playlistId);
        return ResponseEntity.ok().build();
    }


}
