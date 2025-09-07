package com.example.demo.domain.follow.controller;

import com.example.demo.domain.follow.dto.IsUserFollowingResponse;
import com.example.demo.domain.follow.service.PlaylistFollowService;
import com.example.demo.global.security.filter.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Follow", description = "팔로우 관련 API")
@RequestMapping("/main/follow")
public class FollowController {

    private final PlaylistFollowService playlistFollowService;

    @GetMapping("/{playlistId}")
    @Operation(
            summary = "플레이리스트 팔로우 여부 확인 True/False",
            description = "현재 로그인한 사용자가 playlistId를 팔로우 중인지 확인합니다."
    )
    public ResponseEntity<IsUserFollowingResponse> checkFollow(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails me,
            @PathVariable Long playlistId
    ) {
        return ResponseEntity.ok().body(IsUserFollowingResponse.builder()
                .isFollowing(playlistFollowService.isUserFollowing(me.getId(), playlistId))
                .build());
    }


    @PostMapping("/{playlistId}")
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

    @DeleteMapping("/{playlistId}")
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
