package com.example.demo.domain.playlist.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "좋아요한 플레이리스트 응답")
public record LikedPlaylistsResponse(

        @Schema(description = "좋아요한 플레이리스트 총 개수", example = "3")
        int size,

        @Schema(description = "좋아요한 플레이리스트 목록")
        List<LikedPlaylistDto> likedPlaylistDto

) {}
