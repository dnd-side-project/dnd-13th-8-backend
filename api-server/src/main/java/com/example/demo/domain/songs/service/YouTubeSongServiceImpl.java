package com.example.demo.domain.songs.service;

import com.example.demo.domain.songs.controller.YouTubeApiHttp;
import com.example.demo.domain.songs.dto.SongMapper;
import com.example.demo.domain.songs.dto.YouTubeVideoInfoDto;
import com.example.demo.domain.songs.dto.YouTubeVideoResponse;
import java.util.*;
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
     * 프론트가 보낸 링크 순서를 그대로 유지하면서
     * 유효하지 않은 링크는 valid=false로 처리하여 반환합니다.
     */
    public Flux<YouTubeVideoInfoDto> fetchYouTubeInfo(List<String> links) {
        // 유효한 링크들만 videoId 추출
        Map<String, String> linkToVideoId = links.stream()
                .filter(this::isValidYouTubeUrl)
                .collect(Collectors.toMap(
                        Function.identity(),
                        this::extractVideoId
                ));

        String joinedVideoIds = String.join(",", linkToVideoId.values());

        return youTubeApiHttp.getVideoInfo("snippet,contentDetails", joinedVideoIds, apiKey)
                .flatMapMany(response -> {
                    Map<String, YouTubeVideoResponse.Item> itemMap = Optional.ofNullable(response.items())
                            .orElse(List.of()) // null 방지
                            .stream()
                            .collect(Collectors.toMap(
                                    YouTubeVideoResponse.Item::id,
                                    Function.identity()
                            ));

                    List<YouTubeVideoInfoDto> result = new ArrayList<>();

                    for (String link : links) {
                        if (!isValidYouTubeUrl(link)) {
                            result.add(YouTubeVideoInfoDto.invalid(link));
                            continue;
                        }

                        String videoId = linkToVideoId.get(link);
                        YouTubeVideoResponse.Item item = itemMap.get(videoId);

                        if (item == null) {
                            result.add(YouTubeVideoInfoDto.invalid(link));
                            continue;
                        }

                        String title = item.snippet().title();
                        String thumbnailUrl = item.snippet().thumbnails().high().url();
                        String duration = item.contentDetails().duration();

                        result.add(YouTubeVideoInfoDto.valid(link, title, thumbnailUrl, duration));
                    }
                    return Flux.fromIterable(result);
                });
    }

    private boolean isValidYouTubeUrl(String url) {
        return url != null && url.matches("^(https?://)?(www\\.)?(youtube\\.com/watch\\?v=|youtu\\.be/)[\\w-]{11}.*$");
    }

    private String extractVideoId(String url) {
        if (url.contains("youtube.com")) {
            return UriComponentsBuilder.fromUriString(url).build().getQueryParams().getFirst("v");
        } else if (url.contains("youtu.be")) {
            String path = UriComponentsBuilder.fromUriString(url).build().getPath(); // /VIDEO_ID
            return path != null ? path.replaceFirst("/", "") : null;
        }
        return null;
    }
}
