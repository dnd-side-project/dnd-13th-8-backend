package com.example.demo.domain.playlist.dto.search;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "검색 결과 중 사용자 대표 플레이리스트 항목")
public record UserSearchDto(

        @Schema(description = "플레이리스트 제작자 ID", example = "user-123")
        String creatorId,

        @Schema(description = "플레이리스트 제작자 닉네임", example = "playlist_maker")
        String creatorNickname,

        @Schema(description = "사용자 프로필 이미지 URL", example = "https://cdn.example.com/profile/user-uuid-1234.jpg")
        String userProfileImageUrl,

        @Schema(description = "대표 플레이리스트 ID", example = "2001")
        Long playlistId,

        @Schema(description = "대표 플레이리스트 이름", example = "내가 좋아하는 감성곡")
        String playlistName

) implements SearchItem {

    @Override
    @Schema(description = "검색 결과 타입", example = "USER")
    public String getType() {
        return "USER";
    }
}
