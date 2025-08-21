package com.example.demo.domain.playlist.dto;

import com.example.demo.domain.song.dto.SongResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "플레이리스트 + 곡 목록 응답")
public record PlaylistWithSongsResponse(

        @Schema(description = "플레이리스트 ID", example = "101")
        Long playlistId,

        @Schema(description = "플레이리스트에 포함된 곡 목록")
        List<SongResponseDto> songs
) {}
