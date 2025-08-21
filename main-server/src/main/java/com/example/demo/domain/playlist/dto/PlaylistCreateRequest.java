package com.example.demo.domain.playlist.dto;

import com.example.demo.domain.song.dto.YouTubeVideoInfoDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record PlaylistCreateRequest(
        @NotBlank String name, // 플레이리스트 이름
        @NotNull PlaylistGenre genre,
        boolean isRepresentative,
        @NotEmpty List<YouTubeVideoInfoDto> songs
) {}