package com.example.demo.domain.recommendation.dto;

import com.example.demo.domain.playlist.dto.PlaylistGenre;
import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "추천된 플레이리스트 카드 응답")
public record RecommendedPlaylistResponse(

        @Schema(description = "추천 플레이리스트 ID", example = "101")
        Long playlistId,

        @Schema(description = "플레이리스트 제목", example = "여름 드라이브 히트곡 모음")
        String title,

        @Schema(description = "플레이리스트 제작자 닉네임", example = "music_lover")
        String nickName,

        @Schema(description = "플레이리스트 장르", example = "POP")
        PlaylistGenre genres,

        @Schema(description = "조회수", example = "4521")
        Long visitCount
) {
    @QueryProjection
    public RecommendedPlaylistResponse {}
}
