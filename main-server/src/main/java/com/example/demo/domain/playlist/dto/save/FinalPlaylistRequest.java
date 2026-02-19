package com.example.demo.domain.playlist.dto.save;

import com.example.demo.domain.cd.dto.request.SaveCdRequest;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "플레이리스트 생성 요청 DTO")
public record FinalPlaylistRequest(
        @Schema(description = "CD 저장")
        SaveCdRequest saveCdRequest) {
}
