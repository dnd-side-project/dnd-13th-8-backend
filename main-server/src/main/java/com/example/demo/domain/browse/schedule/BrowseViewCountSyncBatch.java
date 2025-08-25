package com.example.demo.domain.browse.schedule;

import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.playlist.repository.PlaylistRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class BrowseViewCountSyncBatch {

    private static final String VIEW_COUNT_PREFIX = "BROWSE_VIEW_COUNT:";
    private final StringRedisTemplate redisTemplate;
    private final PlaylistRepository playlistRepository;

    @Scheduled(cron = "0 0 4 * * *", zone = "Asia/Seoul")
    public void syncViewCountsToDatabase() {
        log.info("[Batch] Browse 조회수 동기화 시작: {}", LocalDateTime.now());

        // Redis에 저장된 모든 view count 키 가져오기
        Set<String> keys = redisTemplate.keys(VIEW_COUNT_PREFIX + "*");

        if (keys == null || keys.isEmpty()) {
            log.info("[Batch] 동기화할 조회수가 없습니다.");
            return;
        }

        List<Playlist> playlistsToUpdate = keys.stream()
                .map(key -> {
                    Long playlistId = parsePlaylistId(key);
                    String value = redisTemplate.opsForValue().get(key);

                    if (playlistId == null || value == null) return null;

                    Playlist playlist = playlistRepository.findById(playlistId).orElse(null);
                    if (playlist == null) return null;

                    long redisCount = Long.parseLong(value);
                    playlist.addVisitCount(redisCount); // 기존 count + redis count 누적

                    return playlist;
                })
                .filter(p -> p != null)
                .collect(Collectors.toList());

        playlistRepository.saveAll(playlistsToUpdate);

        // 동기화 후 Redis 키 삭제
        redisTemplate.delete(keys);

        log.info("[Batch] Browse 조회수 동기화 완료. 동기화된 개수: {}", playlistsToUpdate.size());
    }

    private Long parsePlaylistId(String redisKey) {
        try {
            return Long.parseLong(redisKey.replace(VIEW_COUNT_PREFIX, ""));
        } catch (Exception e) {
            log.warn("파싱 실패: {}", redisKey);
            return null;
        }
    }
}
