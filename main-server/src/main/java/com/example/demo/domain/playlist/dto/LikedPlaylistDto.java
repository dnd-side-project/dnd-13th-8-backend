package com.example.demo.domain.playlist.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "좋아요한 플레이리스트 정보")
public record LikedPlaylistDto(

        @Schema(description = "내가 좋아요 누른 플레이리스트 에 제작자 총 수", example = "42")
        int myLikeCreator,

        @Schema(description = "플레이리스트 제작자 ID", example = "user-123")
        String creatorId,

        @Schema(description = "플레이리스트 제작자 닉네임", example = "playlist_maker")
        String creatorNickname

) {}
