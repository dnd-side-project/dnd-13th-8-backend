package com.example.demo.domain.follow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "팔로우한 플레이리스트 응답")
public record FollowPlaylistsResponse(

        @Schema(description = "팔로워 한 플레이리스트 총 개수", example = "3")
        int size,

        @Schema(description = "팔로워 한 플레이리스트 목록")
        List<FollowPlaylistDto> followPlaylistDto

) {}
