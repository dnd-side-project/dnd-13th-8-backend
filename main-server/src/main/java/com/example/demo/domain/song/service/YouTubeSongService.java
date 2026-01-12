package com.example.demo.domain.song.service;

import com.example.demo.domain.song.dto.api.YouTubeApiVideoDto;
import java.util.List;

public interface YouTubeSongService {

    List<YouTubeApiVideoDto> fetchYouTubeInfo(List<String> links);
}