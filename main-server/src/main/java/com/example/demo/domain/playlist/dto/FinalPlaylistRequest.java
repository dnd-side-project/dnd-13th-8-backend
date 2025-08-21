package com.example.demo.domain.playlist.dto;

import com.example.demo.domain.cd.dto.request.SaveCdRequestDto;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "플레이리스트 생성 요청 DTO")
public record FinalPlaylistRequest(
        @Schema(description = "플레이리스트 테마", example = "여름/운동/집중")
        String theme,

        @Schema(description = "CD 저장")
        SaveCdRequestDto saveCdRequestDto) {
}
