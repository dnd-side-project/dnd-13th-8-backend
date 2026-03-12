package com.example.demo.domain.playlist.controller;

import com.example.demo.domain.playlist.dto.common.PlaylistCoverResponse;
import com.example.demo.domain.playlist.dto.common.PlaylistSortOption;
import com.example.demo.domain.playlist.dto.feed.CarouselPlaylistResponse;
import com.example.demo.domain.playlist.dto.feed.CarouselRequest;
import com.example.demo.domain.playlist.dto.feed.FeedPlaylistResponse;
import com.example.demo.domain.playlist.service.carousel.PlaylistCarouselService;
import com.example.demo.domain.playlist.service.feed.PlaylistFeedService;
import com.example.demo.global.paging.BiCursorPageResponse;
import com.example.demo.global.paging.CursorPageResponse;
import com.example.demo.global.security.filter.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/main/playlist/feed")
@RequiredArgsConstructor
@Tag(name = "Playlist - Feed", description = "피드 플레이리스트 목록 API")
@SecurityRequirement(name = "bearerAuth")
public class PlaylistFeedController {

    private final PlaylistFeedService playlistFeedService;
    private final PlaylistCarouselService playlistCarouselService;

    @Operation(
            summary = "피드 플레이리스트 목록 조회",
            description = "정렬 옵션(POPULAR/RECENT)에 맞춰 피드 플레이리스트 목록을 조회합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "피드 플레이리스트 목록",
            content = @Content(schema = @Schema(implementation = FeedPlaylistResponse.class))
    )
    @GetMapping("/{shareCode}")
    public ResponseEntity<CursorPageResponse<PlaylistCoverResponse, Long>> getFeedPlaylists(
            @AuthenticationPrincipal CustomUserDetails me,
            @PathVariable String shareCode,
            @RequestParam(defaultValue = "POPULAR") PlaylistSortOption sort,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return ResponseEntity.ok(
                playlistFeedService.getPlaylistsSorted(shareCode, me.getId(), sort, cursor, limit)
        );
    }

    @Operation(
            summary = "피드 좋아요한 플레이리스트 목록 조회",
            description = "정렬 옵션(POPULAR/RECENT)에 맞춰 특정 사용자가 좋아요한 플레이리스트를 커서 기반으로 조회합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "피드 플레이리스트 목록",
            content = @Content(schema = @Schema(implementation = FeedPlaylistResponse.class))
    )
    @GetMapping("/{shareCode}/likes")
    public ResponseEntity<CursorPageResponse<PlaylistCoverResponse, Long>> getLikedPlaylists(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails me,
            @PathVariable String shareCode,
            @RequestParam(defaultValue = "POPULAR") PlaylistSortOption sort,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return ResponseEntity.ok(
                playlistFeedService.getLikedPlaylistsSorted(shareCode, me.getId(), sort, cursor, limit)
        );
    }

    @Operation(
            summary = "피드 플레이리스트 캐러셀 조회",
            description = "피드 플레이리스트를 anchorId를 기준으로 데이터를 조회하거나, 방향(direction)과 커서(cursor)로 추가 로딩합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "피드 플레이리스트 캐러셀",
            content = @Content(schema = @Schema(implementation = CarouselPlaylistResponse.class))
    )
    @GetMapping("/{shareCode}/carousel")
    public ResponseEntity<BiCursorPageResponse<PlaylistCoverResponse, Long>> getFeedPlaylistsCarousel(
            @AuthenticationPrincipal CustomUserDetails me,
            @PathVariable String shareCode,
            @Valid @ModelAttribute CarouselRequest request
    ) {
        if (request.anchorId() != null) {
            return ResponseEntity.ok(
                    playlistCarouselService.getFeedCarousel(
                            shareCode,
                            me.getId(),
                            request.sort(),
                            request.anchorId(),
                            request.limit()
                    )
            );
        }

        return ResponseEntity.ok(
                playlistCarouselService.getFeedCarouselMore(
                        shareCode,
                        me.getId(),
                        request.sort(),
                        request.direction(),
                        request.cursor(),
                        request.limit()
                )
        );
    }

    @Operation(
            summary = "피드 좋아요한 플레이리스트 캐러셀 조회",
            description = "피드 좋아요한 플레이리스트를 anchorId 기준으로 데이터를 조회하거나, 방향(direction)과 커서(cursor)로 추가 로딩합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "피드 좋아요한 플레이리스트 캐러셀",
            content = @Content(schema = @Schema(implementation = CarouselPlaylistResponse.class))
    )
    @GetMapping("/{shareCode}/likes/carousel")
    public ResponseEntity<BiCursorPageResponse<PlaylistCoverResponse, Long>> getLikedPlaylistsCarousel(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails me,
            @PathVariable String shareCode,
            @Valid @ModelAttribute CarouselRequest request
    ) {
        if (request.anchorId() != null) {
            return ResponseEntity.ok(
                    playlistCarouselService.getLikedCarousel(
                            shareCode,
                            me.getId(),
                            request.sort(),
                            request.anchorId(),
                            request.limit()
                    )
            );
        }

        return ResponseEntity.ok(
                playlistCarouselService.getLikedCarouselMore(
                        shareCode,
                        me.getId(),
                        request.sort(),
                        request.direction(),
                        request.cursor(),
                        request.limit()
                )
        );
    }
}
