package com.example.demo.domain.playlist.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "플레이리스트 검색 결과 DTO")
public record PlaylistSearchResponse(

        @Schema(description = "플레이리스트 ID", example = "101")
        Long id,

        @Schema(description = "플레이리스트 제목", example = "새벽 집중용 Lofi 모음")
        String playlistName,

        @Schema(description = "플레이리스트 제작자 닉네임", example = "lofi_creator")
        String ownerNickname,

        @Schema(description = "플레이리스트 조회 수", example = "1583")
        Long visitCount

) {}
