package com.example.demo.domain.song.controller;

import com.example.demo.domain.song.dto.YouTubeVideoInfoDto;
import com.example.demo.domain.song.service.YouTubeSongService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/api/playlist/songs")
@RequiredArgsConstructor
public class YouTubeController {

    private final YouTubeSongService songService;

    /**
     * 유튜브 링크 리스트를 받아 영상 정보를 조회합니다.
     * - 단건도 리스트로 받을 수 있습니다.
     */
    @PostMapping()
    public Flux<YouTubeVideoInfoDto> previewFromLinks(
            @RequestBody @Valid @NotEmpty List<@NotEmpty String> youtubeLinks
    ) {
        return songService.fetchYouTubeInfo(youtubeLinks);
    }

}
