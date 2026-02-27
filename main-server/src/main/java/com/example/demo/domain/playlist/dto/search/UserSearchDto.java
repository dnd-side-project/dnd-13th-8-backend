package com.example.demo.domain.playlist.dto.search;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "검색 결과 중 사용자 항목")
public record UserSearchDto(

        @Schema(description = "검색 결과 타입", example = "USER")
        SearchType type,

        @Schema(description = "유저 아이디", example = "user-1234")
        String userId,

        @Schema(description = "프로필 아이디", example = "jigu_jelly")
        String shareCode,

        @Schema(description = "닉네임", example = "기타치는-은하-1234")
        String nickname,

        @Schema(description = "프로필 이미지", example = "https://cdn.example.com/profile/user-uuid-1234.jpg")
        String profileUrl

) implements SearchItem {

}
