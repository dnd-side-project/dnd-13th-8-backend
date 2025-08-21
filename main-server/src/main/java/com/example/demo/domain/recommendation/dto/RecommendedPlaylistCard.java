package com.example.demo.domain.recommendation.dto;

import com.example.demo.domain.playlist.dto.SongDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "추천 플레이리스트 카드 정보")
public record RecommendedPlaylistCard(

        @Schema(description = "플레이리스트 ID", example = "101")
        Long playlistId,

        @Schema(description = "플레이리스트 이름", example = "비 오는 날 듣기 좋은 발라드")
        String playlistName,

        @Schema(description = "플레이리스트 제작자 닉네임", example = "ballad_master")
        String ownerName,

        @Schema(description = "대표 플레이리스트 여부", example = "false")
        boolean isRepresentative,

        @Schema(description = "추천된 곡 목록 (미리보기용)")
        List<SongDto> songs

) {}
