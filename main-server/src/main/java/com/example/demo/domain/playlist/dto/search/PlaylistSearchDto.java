package com.example.demo.domain.playlist.dto.search;

import com.example.demo.domain.cd.dto.response.CdResponse;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "검색 결과 중 플레이리스트 항목")
public record PlaylistSearchDto(

        @Schema(description = "검색 결과 타입", example = "PLAYLIST")
        SearchType type,

        @Schema(description = "플레이리스트 ID", example = "1001")
        Long playlistId,

        @Schema(description = "플레이리스트 이름", example = "감성 힙합 모음집")
        String playlistName,

        @Schema(description = "플레이리스트 제작자 ID", example = "user-123")
        String creatorId,

        @Schema(description = "플레이리스트 제작자 닉네임", example = "홍길동")
        String creatorNickname,

        @Schema(description = "cd 정보")
        CdResponse cdResponse

) implements SearchItem {

    public PlaylistSearchDto withCdResponse(CdResponse cdResponse) {
        return new PlaylistSearchDto(
                this.type,
                this.playlistId,
                this.playlistName,
                this.creatorId,
                this.creatorNickname,
                cdResponse
        );
    }
}
