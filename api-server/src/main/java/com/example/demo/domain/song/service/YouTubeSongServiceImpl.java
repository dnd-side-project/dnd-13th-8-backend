package com.example.demo.domain.song.service;

import com.example.demo.domain.song.controller.YouTubeApiHttp;
import com.example.demo.domain.song.dto.SongMapper;
import com.example.demo.domain.song.dto.YouTubeVideoInfoDto;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class YouTubeSongServiceImpl implements YouTubeSongService {

    private final YouTubeApiHttp youTubeApiHttp;

    @Value("${youtube.api.key}")
    private String apiKey;

    /**
     * 여러 유튜브 링크를 받아 영상 정보를 조회합니다.
     */
    public Flux<YouTubeVideoInfoDto> fetchYouTubeInfo(List<String> links) {
        Map<String, String> linkToVideoId = links.stream()
                .filter(this::isValidYouTubeUrl)
                .collect(Collectors.toMap(
                        Function.identity(),
                        this::extractVideoId
                ));

        String joinedVideoIds = String.join(",", linkToVideoId.values());

        return youTubeApiHttp.getVideoInfo("snippet,contentDetails", joinedVideoIds, apiKey)
                .flatMapMany(response -> {
                    if (response.items() == null || response.items().isEmpty()) {
                        return Flux.error(new IllegalArgumentException("유효한 영상이 없습니다."));
                    }

                    return Flux.fromIterable(response.items())
                            .map(item -> {
                                String videoId = item.id();
                                String originalLink = getLinkFromVideoId(linkToVideoId, videoId);
                                return SongMapper.toDto(item, originalLink);
                            });
                });

    }


    private boolean isValidYouTubeUrl(String url) {
        return url != null && url.matches("^(https?://)?(www\\.)?(youtube\\.com/watch\\?v=|youtu\\.be/)[\\w-]{11}.*$");
    }

    private String extractVideoId(String url) {
        if (url.contains("youtube.com")) {
            return UriComponentsBuilder.fromUriString(url).build().getQueryParams().getFirst("v");
        } else if (url.contains("youtu.be")) {
            return url.substring(url.lastIndexOf("/") + 1);
        }
        return null;
    }

    private String getLinkFromVideoId(Map<String, String> map, String videoId) {
        return map.entrySet().stream()
                .filter(entry -> entry.getValue().equals(videoId))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse("");
    }

}
