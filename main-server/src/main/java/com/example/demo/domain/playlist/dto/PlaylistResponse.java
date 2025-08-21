package com.example.demo.domain.playlist.dto;

import com.example.demo.domain.playlist.entity.Playlist;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "플레이리스트 응답 DTO")
public record PlaylistResponse(

        @Schema(description = "플레이리스트 ID", example = "101")
        Long id,

        @Schema(description = "플레이리스트 이름", example = "여름 드라이브 플레이리스트")
        String name,

        @Schema(description = "대표 플레이리스트 여부", example = "true")
        boolean isRepresentative,

        @Schema(description = "플레이리스트 조회 수", example = "1200")
        Long visitCount
) {
    public static PlaylistResponse from(Playlist playlist) {
        return PlaylistResponse.builder()
                .id(playlist.getId())
                .name(playlist.getName())
                .isRepresentative(playlist.getIsRepresentative())
                .visitCount(playlist.getVisitCount())
                .build();
    }
}
