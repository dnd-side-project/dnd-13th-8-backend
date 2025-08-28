package com.example.demo.domain.browse.service;

import com.example.demo.domain.browse.dto.PlaylistViewCountDto;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BrowseViewCountService {

    private final StringRedisTemplate redisTemplate;

    private static final String ZSET_KEY_PREFIX = "BROWSE_HEARTBEAT:";
    private static final String VIEW_COUNT_KEY_PREFIX = "BROWSE_VIEW_COUNT:";

    // start 시 서버 저장 안 해도 되지만 추후 로그나 추적용으로 쓸 수 있음
    public void logHeartbeatStart(String userId, Long playlistId) {
        // 여기선 아무 것도 하지 않음 (기획에 따라 추후 로깅용으로 확장 가능)
    }

    // 15초 이상 재생 확인되면 이 메서드 호출
    public void confirmView(String userId, Long playlistId) {
        String zsetKey = ZSET_KEY_PREFIX + playlistId;
        String countKey = VIEW_COUNT_KEY_PREFIX + playlistId;

        // 이미 본 적 있으면 무시 (중복 조회수 방지)
        Boolean alreadyViewed = redisTemplate.opsForZSet().score(zsetKey, userId) != null;
        if (!Boolean.TRUE.equals(alreadyViewed)) {
            double now = System.currentTimeMillis();

            // ZSET: 유저가 본 시간 기록 (조회수 집계용)
            redisTemplate.opsForZSet().add(zsetKey, userId, now);
            redisTemplate.expire(zsetKey, Duration.ofDays(1));

            // 조회수 1 증가
            redisTemplate.opsForValue().increment(countKey);
        }
    }

    /**
     * Redis에 저장된 조회수를 반환합니다.
     * 값이 없으면 기본값 0을 반환합니다.
     */
    public List<PlaylistViewCountDto> getViewCounts(List<Long> playlistIds) {
        List<PlaylistViewCountDto> result = new ArrayList<>();
        if (playlistIds == null || playlistIds.isEmpty()) {
            return result;
        }

        List<String> keys = playlistIds.stream()
                .map(id -> "BROWSE_VIEW_COUNT:" + id)
                .toList();

        List<String> values = redisTemplate.opsForValue().multiGet(keys);

        for (int i = 0; i < playlistIds.size(); i++) {
            Long playlistId = playlistIds.get(i);
            String value = (i < values.size()) ? values.get(i) : null;

            Long viewCount = 0L;
            if (value != null) {
                try {
                    viewCount = Long.parseLong(value);
                } catch (NumberFormatException e) {
                    log.warn("조회수 파싱 실패: playlistId={}, value={}", playlistId, value);
                }
            }

            result.add(new PlaylistViewCountDto(playlistId, viewCount));
        }

        return result;
    }

}
