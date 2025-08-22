package com.example.demo.domain.songs.controller;

import com.example.demo.domain.songs.dto.YouTubeVideoResponse;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import reactor.core.publisher.Mono;

@HttpExchange(url = "https://www.googleapis.com/youtube/v3")
public interface YouTubeApiHttp {

        @GetExchange("/videos")
        Mono<YouTubeVideoResponse> getVideoInfo(
                @RequestParam("part") String part,       // 예: "snippet,contentDetails"
                @RequestParam("id") String videoIds,      // comma-separated videoId
                @RequestParam("key") String apiKey
        );
    }

