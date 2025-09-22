package com.example.demo.domain.follow.controller;

import com.example.demo.domain.follow.dto.response.IsUserFollowingResponse;
import com.example.demo.domain.follow.service.FollowService;
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

    private final FollowService followService;

    @GetMapping("/{followeeId}")
    @Operation(
            summary = "팔로우 여부 확인 True/False",
            description = "현재 로그인한 사용자가 해당 유저를 팔로우 중인지 확인합니다."
    )
    public ResponseEntity<IsUserFollowingResponse> checkFollow(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails me,
            @PathVariable String followeeId
    ) {
        return ResponseEntity.ok().body(IsUserFollowingResponse.builder()
                .isFollowing(followService.isUserFollowing(me.getId(), followeeId))
                .build());
    }


    @PostMapping("/{followeeId}")
    @Operation(
            summary = "팔로우",
            description = "현재 로그인한 사용자가 해당 유저를 팔로우합니다. 중복 팔로우는 무시됩니다."
    )
    public ResponseEntity<Void> followPlaylist(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails me,
            @PathVariable String followeeId
    ) {
        followService.follow(me.getId(), followeeId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{followeeId}")
    @Operation(
            summary = "팔로우 취소",
            description = "현재 로그인한 사용자가 해당 유저를 팔로우 취소합니다. 팔로우 상태가 아니라면 아무 동작도 하지 않습니다."
    )
    public ResponseEntity<Void> unfollowPlaylist(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails me,
            @PathVariable String followeeId
    ) {
        followService.unfollow(me.getId(), followeeId);
        return ResponseEntity.ok().build();
    }
}
