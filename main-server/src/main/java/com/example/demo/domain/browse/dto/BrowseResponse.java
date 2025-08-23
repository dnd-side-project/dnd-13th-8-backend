package com.example.demo.domain.browse.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "커서 기반 플레이리스트 응답")
public record BrowseResponse(

        @Schema(description = "플레이리스트 목록", required = true)
        List<BrowsePlaylistDto> playlists,

        @Schema(description = "다음 커서. -1이면 마지막 페이지", example = "15")
        int nextCursor,

        @Schema(description = "다음 페이지 존재 여부", example = "true")
        boolean hasNext
) {}
