package com.example.demo.domain.browse.controller;

import com.example.demo.domain.browse.dto.BrowsePlaylistDto;
import com.example.demo.domain.browse.dto.BrowseResponse;
import com.example.demo.domain.browse.service.BrowsePlaylistService;
import com.example.demo.domain.follow.service.PlaylistFollowService;
import com.example.demo.global.security.filter.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.List;
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
    @Operation(
            summary = "셔플된 플레이리스트 목록 조회 (둘러보기)",
            description = "매일 새벽 3시에 셔플된 대표 플레이리스트 카드 20개를 조회합니다."
    )
    public ResponseEntity<BrowseResponse> browsePlaylists(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        BrowseResponse response = browsePlaylistService.getShuffledPlaylists(user.getId());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    @PostMapping("/{playlistId}/follow")
    public ResponseEntity<Void> followPlaylist(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails me,
            @PathVariable Long playlistId) {
        playlistFollowService.follow(me.getId(), playlistId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{playlistId}/follow")
    public ResponseEntity<Void> unfollowPlaylist(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails me,
            @PathVariable Long playlistId) {
        playlistFollowService.unfollow(me.getId(), playlistId);
        return ResponseEntity.ok().build();
    }


}
