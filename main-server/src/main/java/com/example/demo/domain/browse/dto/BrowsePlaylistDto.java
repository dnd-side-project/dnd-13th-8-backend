package com.example.demo.domain.browse.dto;

import com.example.demo.domain.cd.dto.response.CdItemResponse;
import com.example.demo.domain.playlist.dto.common.SongDto;
import com.example.demo.domain.playlist.dto.common.PlaylistDetailWithCreatorResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "플레이리스트 카드 정보")
public record BrowsePlaylistDto(

        @Schema(description = "카드 ID", example = "2")
        Long cardId,

        @Schema(description = "카드 노출 순서 (position)", example = "0")
        int position,

        @Schema(description = "플레이리스트 ID", example = "101")
        Long playlistId,

        @Schema(description = "플레이리스트 이름", example = "잔잔한 피아노곡")
        String playlistName,

        @Schema(description = "장르 이름", example = "JAZZ")
        String genre,

        @Schema(description = "플레이리스트 작성자 정보")
        CreatorDto creator,

        @Schema(description = "플레이리스트 곡 미리보기 목록")
        List<SongDto> songs,

        @Schema(description = "대표 플레이리스트 여부", example = "true")
        boolean representative,

        @Schema(description = "공유 링크", example = "https://deulak.com/share/u1")
        String shareUrl,

        @Schema(description = "CD 이미지 및 위치 정보")
        List<CdItemResponse> cdItems,

        @Schema(description = "전체 곡 재생 시간", example = "09:32")
        String totalTime
) {
    public static BrowsePlaylistDto from(PlaylistDetailWithCreatorResponse detail) {
        return new BrowsePlaylistDto(
                detail.playlistId(),
                0,
                detail.playlistId(),
                detail.playlistName(),
                detail.genre().name(),
                new CreatorDto(
                        detail.creatorId(),
                        detail.creatorNickname()
                ),
                detail.songs(),
                true,
                null,
                detail.cdResponse() != null
                        ? detail.cdResponse().cdItems()
                        : List.of(),
                null
        );
    }

}


