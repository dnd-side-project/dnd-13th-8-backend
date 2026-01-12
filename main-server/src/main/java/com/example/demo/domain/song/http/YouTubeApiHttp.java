package com.example.demo.domain.song.http;

import com.example.demo.domain.song.dto.api.YouTubeApiResponse;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange(url = "https://www.googleapis.com/youtube/v3")
public interface YouTubeApiHttp {

    @GetExchange("/videos")
    YouTubeApiResponse getVideoInfo(
            @RequestParam("part") String part,       // 예: "snippet,contentDetails"
            @RequestParam("id") String videoIds,      // comma-separated videoId
            @RequestParam("key") String apiKey
    );
}