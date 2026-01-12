package com.example.demo.domain.song.service;

import com.example.demo.domain.song.http.YouTubeApiHttp;
import com.example.demo.domain.song.dto.api.YouTubeApiResponse;
import com.example.demo.domain.song.dto.api.YouTubeApiVideoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RequiredArgsConstructor
@Service
public class YoutubeSongServiceImpl implements YouTubeSongService{

    private final YouTubeApiHttp youTubeApiHttp;

    @Value("${youtube.api.key}")
    private String apiKey;

    @Override
    public List<YouTubeApiVideoDto> fetchYouTubeInfo(List<String> links) {
        Map<String, String> linkToVideoId = extractValidLinkToVideoId(links);

        if (linkToVideoId.isEmpty()) {
            return links.stream().map(YouTubeApiVideoDto::invalid)
                    .toList();
        }

        String joinedVideoIds = String.join(",", linkToVideoId.values());

        YouTubeApiResponse response = executeWithRetry(
                () -> youTubeApiHttp.getVideoInfo("snippet,contentDetails", joinedVideoIds, apiKey)
        );

        Map<String, YouTubeApiResponse.Item> itemMap = fetchVideoItemMap(response.items());

        return IntStream.range(0, links.size())
                .mapToObj(i -> {
                    String link = links.get(i);
                    Long orderIndex = (long) (i + 1);
                    String videoId = linkToVideoId.get(link);
                    return mapToDto(link, videoId, itemMap, orderIndex);
                })
                .toList();
    }

    private Map<String, String> extractValidLinkToVideoId(List<String> links) {
        return links.stream()
                .filter(this::isValidYouTubeUrl)
                .collect(Collectors.toMap(
                        Function.identity(),
                        this::extractVideoId,
                        (exist, dup) -> exist,
                        LinkedHashMap::new
                ));
    }

    private Map<String, YouTubeApiResponse.Item> fetchVideoItemMap(List<YouTubeApiResponse.Item> items) {
        return Optional.ofNullable(items)
                .orElse(List.of())
                .stream()
                .collect(Collectors.toMap(
                        YouTubeApiResponse.Item::id,
                        Function.identity()
                ));
    }

    private YouTubeApiVideoDto mapToDto(String link,
                                        String videoId,
                                        Map<String, YouTubeApiResponse.Item> itemMap,
                                        Long orderIndex) {
        if (videoId == null || !itemMap.containsKey(videoId)) {
            return YouTubeApiVideoDto.invalid(link);
        }

        YouTubeApiResponse.Item item = itemMap.get(videoId);

        return YouTubeApiVideoDto.valid(
                link,
                item.snippet().title(),
                item.snippet().thumbnails().high().url(),
                orderIndex,
                item.contentDetails().duration()
        );
    }

    private boolean isValidYouTubeUrl(String url) {
        return url != null && url.matches("^(https?://)?(www\\.|music\\.)?(youtube\\.com/watch\\?v=|youtu\\.be/)[\\w-]{11}.*$");
    }

    private String extractVideoId(String url) {
        if (url.contains("youtube.com")) {
            return UriComponentsBuilder.fromUriString(url).build().getQueryParams().getFirst("v");
        } else if (url.contains("youtu.be")) {
            String path = UriComponentsBuilder.fromUriString(url).build().getPath();
            return path != null ? path.replaceFirst("/", "") : null;
        }
        return null;
    }

    private <T> T executeWithRetry(Callable<T> call) {
        int maxRetries = 3;
        long delayMs = 500;
        long maxDelayMs = 5000;

        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                return call.call();
            } catch (Exception ex) {
                if (!isRetryable(ex) || attempt == maxRetries) {
                    if (ex instanceof RuntimeException re) throw re;
                    throw new RuntimeException(ex);
                }

                long jitter = (long) (Math.random() * 100);
                sleep(delayMs + jitter);
                delayMs = Math.min(delayMs * 2, maxDelayMs);
            }
        }
        throw new IllegalStateException("unreachable");
    }

    private boolean isRetryable(Exception ex) {
        if (ex instanceof IOException) return true;

        if (ex instanceof RestClientResponseException rre) {
            var status = rre.getStatusCode();
            return status.value() == 429 || status.is5xxServerError();
        }

        String msg = ex.getMessage();
        return msg != null && (msg.contains("429") || msg.contains("5"));
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new RuntimeException(e); }
    }
}

