package com.example.demo.domain.playlist.dto.playlistdto;

import com.example.demo.domain.cd.dto.response.CdResponse;
import com.example.demo.domain.playlist.entity.Playlist;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "플레이리스트 응답 DTO")
public record PlaylistResponse(

        @Schema(description = "플레이리스트 ID", example = "101")
        Long playlistId,

        @Schema(description = "플레이리스트 이름", example = "여름 드라이브 플레이리스트")
        String playlistName,

        @Schema(description = "플레이리스트 제작자 이름", example = "여름 드라이브 플레이리스트")
        String creatorNickname,

        @Schema(description = "플레이리스트 공개 여부", example = "true")
        boolean isPublic,

        @Schema(description = "cd 정보")
        CdResponse cdResponse

) {
    public static PlaylistResponse from(Playlist playlist, CdResponse cd) {
        return PlaylistResponse.builder()
                .playlistId(playlist.getId())
                .playlistName(playlist.getName())
                .creatorNickname(playlist.getUsers().getUsername())
                .isPublic(playlist.isPublic())
                .cdResponse(cd)
                .build();
    }

}
