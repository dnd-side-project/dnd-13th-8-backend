package com.example.demo.domain.browse.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "커서 기반 플레이리스트 응답")
public record BrowseResponse(

        @Schema(description = "플레이리스트 목록", required = true)
        List<BrowsePlaylistDto> playlists
) {}
