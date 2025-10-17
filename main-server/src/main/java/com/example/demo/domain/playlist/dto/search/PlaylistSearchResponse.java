package com.example.demo.domain.playlist.dto.search;

import com.example.demo.domain.cd.dto.response.CdResponse;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "플레이리스트 검색 결과 DTO")
public record PlaylistSearchResponse(

        @Schema(description = "플레이리스트 ID", example = "101")
        Long playlistId,

        @Schema(description = "플레이리스트 제목", example = "새벽 집중용 Lofi 모음")
        String playlistName,

        @Schema(description = "플레이리스트 제작자 ID", example = "user-123")
        String creatorId,

        @Schema(description = "플레이리스트 제작자 닉네임", example = "playlist_maker")
        String creatorNickname,

        @Schema(description = "cd 정보")
        CdResponse cdResponse
) {}
