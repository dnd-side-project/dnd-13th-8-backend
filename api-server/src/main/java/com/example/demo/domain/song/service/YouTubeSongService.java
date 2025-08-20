package com.example.demo.domain.song.service;

import com.example.demo.domain.song.dto.SongResponseDto;
import com.example.demo.domain.song.dto.YouTubeVideoInfoDto;
import com.example.demo.domain.song.entity.Song;
import java.util.List;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface YouTubeSongService {

    Mono<List<SongResponseDto>> saveReactiveSongs(List<YouTubeVideoInfoDto> links, Long playlistId);

    Flux<YouTubeVideoInfoDto> fetchYouTubeInfo(List<String> links);
}
