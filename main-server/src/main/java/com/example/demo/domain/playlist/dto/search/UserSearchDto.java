package com.example.demo.domain.playlist.dto.search;

import com.example.demo.domain.playlist.dto.SongDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "검색 결과 중 사용자 대표 플레이리스트 항목")
public record UserSearchDto(

        @Schema(description = "사용자 ID", example = "user-uuid-1234")
        String userId,

        @Schema(description = "사용자 이름", example = "junyeop_dev")
        String username,

        @Schema(description = "사용자 프로필 이미지 URL", example = "https://cdn.example.com/profile/user-uuid-1234.jpg")
        String userProfileImageUrl,

        @Schema(description = "대표 플레이리스트 ID", example = "2001")
        Long playlistId,

        @Schema(description = "대표 플레이리스트 이름", example = "내가 좋아하는 감성곡")
        String playlistName,

        @Schema(description = "대표 플레이리스트에 포함된 곡 목록")
        List<SongDto> songs

) implements SearchItem {

    @Override
    @Schema(description = "검색 결과 타입", example = "USER")
    public String getType() {
        return "USER";
    }
}
