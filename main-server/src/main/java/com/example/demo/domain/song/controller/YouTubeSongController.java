package com.example.demo.domain.song.controller;

import com.example.demo.domain.song.dto.api.YouTubeApiVideoDto;
import com.example.demo.domain.song.service.YouTubeSongService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/playlist/songs")
@RequiredArgsConstructor
@Tag(name = "YouTube", description = "유튜브 영상 정보 조회 API")
public class YouTubeSongController {

    private final YouTubeSongService songService;

    @Operation(
            summary = "유튜브 영상 정보 조회",
            description = """
            유튜브 링크 리스트를 받아 영상 정보를 API로 조회합니다.
            """
    )
    @ApiResponse(
            responseCode = "200",
            description = "유튜브 영상 정보 리스트",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = YouTubeApiVideoDto.class)))
    )
    @PostMapping
    public List<YouTubeApiVideoDto> previewFromLinks(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(array = @ArraySchema(schema = @Schema(example = "[\"https://youtu.be/abc123\", \"https://youtu.be/def456\"]")))
            )
            @RequestBody @Valid @NotEmpty List<@NotEmpty String> youtubeLinks
    ) {
        return songService.fetchYouTubeInfo(youtubeLinks);
    }
}
