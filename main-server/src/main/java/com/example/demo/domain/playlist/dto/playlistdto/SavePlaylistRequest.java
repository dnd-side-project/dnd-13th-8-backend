package com.example.demo.domain.playlist.dto.playlistdto;

import com.example.demo.domain.playlist.dto.PlaylistGenre;
import com.example.demo.domain.song.dto.YouTubeVideoInfoDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "플레이리스트 저장 요청 DTO")
public record SavePlaylistRequest(

        @Schema(description = "플레이리스트 이름", example = "비 오는 날 집중용")
        @NotBlank
        String name,

        @Schema(description = "플레이리스트 장르", example = "SLEEP")
        @NotNull
        PlaylistGenre genre,

        @Schema(description = "공개 여부", example = "true")
        boolean isPublic,

        @Schema(description = "포함할 유튜브 영상 목록")
        @NotEmpty
        List<YouTubeVideoInfoDto> youTubeVideoInfo
) {}
