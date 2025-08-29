package com.example.demo.domain.playlist.dto;

import com.example.demo.domain.cd.dto.request.SaveCdRequestDto;
import io.swagger.v3.oas.annotations.media.Schema;

public record EditFinalPlaylistRequest(Long playlistId, @Schema(description = "CD 저장")
                                       SaveCdRequestDto saveCdRequestDto) {
}
