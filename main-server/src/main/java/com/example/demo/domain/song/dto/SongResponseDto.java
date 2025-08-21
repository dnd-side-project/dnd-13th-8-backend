package com.example.demo.domain.song.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "곡 응답 DTO")
public record SongResponseDto(

        @Schema(description = "곡 ID", example = "501")
        Long id,

        @Schema(description = "플레이리스트 ID", example = "101")
        Long playlistId,  // ⚠️ 엔티티가 아니라 Long ID만 내려주도록 수정 권장

        @Schema(description = "유튜브 영상 URL", example = "https://youtube.com/watch?v=abc123")
        String youtubeUrl,

        @Schema(description = "유튜브 영상 제목", example = "여름에 듣기 좋은 노래 모음")
        String youtubeTitle,

        @Schema(description = "유튜브 영상 썸네일 URL", example = "https://img.youtube.com/vi/abc123/hqdefault.jpg")
        String youtubeThumbnail,

        @Schema(description = "유튜브 영상 길이 (초 단위)", example = "210")
        Long youtubeLength
) {}
