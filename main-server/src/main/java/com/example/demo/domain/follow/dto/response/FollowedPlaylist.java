package com.example.demo.domain.follow.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "팔로우한 플레이리스트 정보")
public record FollowedPlaylist(

        @Schema(description = "플레이리스트 제작자 ID", example = "user-123")
        String creatorId,

        @Schema(description = "플레이리스트 제작자 에 대표 플레이스트 Id", example = "user-123")
        String creatorPlaylistId,

        @Schema(description = "플레이리스트 제작자 닉네임", example = "playlist_maker")
        String creatorNickname,

        @Schema(description = "플레이리스트 제작자 이미지",  example = "https://cdn.example.com/profile/user-uuid-1234.jpg")
        String creatorProfileImageUrl

) {}
