package com.example.demo.domain.browse.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Schema(description = "여러 플레이리스트 조회수 요청 DTO")
public record PlaylistViewCountRequest(

        @Schema(description = "조회수를 가져올 플레이리스트 ID 목록", example = "[101, 102, 103]")
        @NotEmpty(message = "playlistIds는 비어 있을 수 없습니다.")
        List<Long> playlistIds

) {}
