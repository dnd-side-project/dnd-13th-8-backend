package com.example.demo.domain.songs.service;

import com.example.demo.domain.songs.dto.YouTubeVideoInfoDto;

import java.util.List;
import reactor.core.publisher.Flux;

public interface YouTubeSongService {


    Flux<YouTubeVideoInfoDto> fetchYouTubeInfo(List<String> links);
}
