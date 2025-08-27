package com.example.demo.domain.playlist.dto.search;

import com.example.demo.domain.playlist.dto.SongDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "검색 결과 중 플레이리스트 항목")
public record PlaylistSearchDto(

        @Schema(description = "플레이리스트 ID", example = "1001")
        Long playlistId,

        @Schema(description = "플레이리스트 이름", example = "감성 힙합 모음집")
        String playlistName,

        @Schema(description = "플레이리스트 생성 유저 ID", example = "user-uuid-1234")
        String userId,

        @Schema(description = "플레이리스트 생성 유저 이름", example = "junyeop_dev")
        String username,

        @Schema(description = "플레이리스트에 포함된 곡 목록")
        List<SongDto> songs

) implements SearchItem {

    @Override
    @Schema(description = "검색 결과 타입", example = "PLAYLIST")
    public String getType() {
        return "PLAYLIST";
    }
}
