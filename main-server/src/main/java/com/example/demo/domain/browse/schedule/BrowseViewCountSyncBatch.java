package com.example.demo.domain.browse.schedule;

import com.example.demo.domain.playlist.repository.PlaylistRepository;
import java.util.List;
import java.util.Set;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class BrowseViewCountSyncBatch {

    private static final String VIEW_COUNT_PREFIX = "BROWSE_VIEW_COUNT:";
    private final StringRedisTemplate redisTemplate;

    @PostConstruct
    public void cleanup() {
        try {
            Set<String> keys = redisTemplate.keys(VIEW_COUNT_PREFIX + "*");

            if (keys == null || keys.isEmpty()) {
                log.info("[Cleanup] 삭제할 Browse Redis 키가 없습니다.");
                return;
            }

            log.warn("[Cleanup] 삭제 대상 키 개수={}", keys.size());

            int count = 0;
            for (String key : keys) {
                log.warn("[Cleanup][KEY][{}] {}", count++, key);
            }

            deleteInChunks(keys, 500);

            log.warn("[Cleanup] Browse Redis 키 삭제 완료. total={}", count);
        } catch (Exception e) {
            log.error("[Cleanup] Browse Redis 키 삭제 중 오류", e);
        }
    }

    private void deleteInChunks(Set<String> keys, int batchSize) {
        List<String> keyList = List.copyOf(keys);
        for (int i = 0; i < keyList.size(); i += batchSize) {
            List<String> batch = keyList.subList(i, Math.min(i + batchSize, keyList.size()));
            redisTemplate.delete(batch);
            log.info("[Cleanup] deletedBatchSize={}", batch.size());
        }
    }
}
