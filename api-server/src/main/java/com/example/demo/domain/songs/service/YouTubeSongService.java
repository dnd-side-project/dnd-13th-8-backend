package com.example.demo.domain.songs.service;

import com.example.demo.domain.songs.dto.SongResponseDto;
import com.example.demo.domain.songs.dto.YouTubeVideoInfoDto;

import java.util.List;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface YouTubeSongService {


    Flux<YouTubeVideoInfoDto> fetchYouTubeInfo(List<String> links);
}
