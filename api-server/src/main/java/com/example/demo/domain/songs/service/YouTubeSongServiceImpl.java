package com.example.demo.domain.songs.service;

import com.example.demo.domain.songs.controller.YouTubeApiHttp;
import com.example.demo.domain.songs.dto.YouTubeVideoInfoDto;
import com.example.demo.domain.songs.dto.YouTubeVideoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    @Override
    public Flux<YouTubeVideoInfoDto> fetchYouTubeInfo(List<String> links) {
        Map<String, String> linkToVideoId = extractValidLinkToVideoId(links);
        String joinedVideoIds = String.join(",", linkToVideoId.values());

        return youTubeApiHttp.getVideoInfo("snippet,contentDetails", joinedVideoIds, apiKey)
                .flatMapMany(response -> {
                    Map<String, YouTubeVideoResponse.Item> itemMap = fetchVideoItemMap(response.items());

                    List<YouTubeVideoInfoDto> result =
                            IntStream.range(0, links.size())
                                    .mapToObj(i -> {
                                        String link = links.get(i);
                                        Long orderIndex = (long) (i + 1); // 요청 순서(1-based)
                                        String videoId = linkToVideoId.get(link);
                                        return mapToDto(link, videoId, itemMap, orderIndex);
                                    })
                                    .toList();

                    return Flux.fromIterable(result);
                });
    }

    /**
     * 유효한 YouTube 링크에서 videoId 추출
     */
    private Map<String, String> extractValidLinkToVideoId(List<String> links) {
        return links.stream()
                .filter(this::isValidYouTubeUrl)
                .collect(Collectors.toMap(
                        Function.identity(),
                        this::extractVideoId,
                        (exist, dup) -> exist,      // 중복 시 첫 값 유지
                        LinkedHashMap::new          // 삽입 순서 보존
                ));
    }

    /**
     * YouTube API 응답 item 리스트를 Map<videoId, item> 형태로 변환
     */
    private Map<String, YouTubeVideoResponse.Item> fetchVideoItemMap(List<YouTubeVideoResponse.Item> items) {
        return Optional.ofNullable(items)
                .orElse(List.of())
                .stream()
                .collect(Collectors.toMap(
                        YouTubeVideoResponse.Item::id,
                        Function.identity()
                ));
    }

    /**
     * 링크와 videoId, itemMap을 바탕으로 Dto 변환
     */
    private YouTubeVideoInfoDto mapToDto(String link,
                                         String videoId,
                                         Map<String, YouTubeVideoResponse.Item> itemMap,
                                         Long orderIndex) {
        if (videoId == null || !itemMap.containsKey(videoId)) {
            return YouTubeVideoInfoDto.invalid(link);
        }

        YouTubeVideoResponse.Item item = itemMap.get(videoId);

        return YouTubeVideoInfoDto.valid(
                link,
                item.snippet().title(),
                item.snippet().thumbnails().high().url(),
                orderIndex,
                item.contentDetails().duration()
        );
    }

    /**
     * youtube.com / music.youtube.com / youtu.be 모두 허용
     */
    private boolean isValidYouTubeUrl(String url) {
        return url != null && url.matches("^(https?://)?(www\\.|music\\.)?(youtube\\.com/watch\\?v=|youtu\\.be/)[\\w-]{11}.*$");
    }

    /**
     * URL에서 videoId 추출
     */
    private String extractVideoId(String url) {
        if (url.contains("youtube.com")) {
            return UriComponentsBuilder.fromUriString(url).build().getQueryParams().getFirst("v");
        } else if (url.contains("youtu.be")) {
            String path = UriComponentsBuilder.fromUriString(url).build().getPath();
            return path != null ? path.replaceFirst("/", "") : null;
        }
        return null;
    }
}
