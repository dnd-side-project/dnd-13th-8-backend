package com.example.demo.domain.like.controller;

import com.example.demo.domain.like.dto.IsLikedResponse;
import com.example.demo.domain.like.service.LikesService;
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
@Tag(name = "Likes", description = "좋아요 관련 API")
@RequestMapping("/main/likes")
public class LikesController {

    private final LikesService likesService;

    @GetMapping("/{playlistId}")
    @Operation(
            summary = "좋아요 여부 확인 True/False",
            description = "현재 로그인한 사용자가 해당 플레이리스트를 좋아요 중인지 확인합니다."
    )
    public ResponseEntity<IsLikedResponse> checkLike(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails me,
            @PathVariable Long playlistId
    ) {
        return ResponseEntity.ok().body(IsLikedResponse.builder()
                .isLiked(likesService.isPlaylistLiked(me.getId(), playlistId))
                .build());
    }

    @PostMapping("/{playlistId}")
    @Operation(
            summary = "좋아요",
            description = "현재 로그인한 사용자가 해당 플레이리스트를 좋아요합니다. 중복 좋아요는 무시됩니다."
    )
    public ResponseEntity<Void> like(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails me,
            @PathVariable Long playlistId
    ) {
        likesService.like(me.getId(), playlistId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{playlistId}")
    @Operation(
            summary = "좋아요 취소",
            description = "현재 로그인한 사용자가 해당 플레이리스트의 좋아요를 취소합니다. 좋아요 상태가 아니라면 아무 동작도 하지 않습니다."
    )
    public ResponseEntity<Void> unlike(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails me,
            @PathVariable Long playlistId
    ) {
        likesService.unlike(me.getId(), playlistId);
        return ResponseEntity.ok().build();
    }

}
