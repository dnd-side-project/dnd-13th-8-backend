package com.example.demo.global.auth.refresh.store;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Collections;

/**
 * Redis 기반 RefreshTokenStore 구현체
 * - Lua 스크립트 Bean을 주입받아 원자적 연산 실행
 * - 여기서는 순수 I/O 프리미티브만 제공 (비즈니스 로직 없음)
 */
@Repository
@RequiredArgsConstructor
public class RedisRefreshTokenStore implements RefreshTokenStore {

    private final StringRedisTemplate redis;
    private final DefaultRedisScript<String> consumeScript;   // [A] JTI 소비 스크립트
    private final DefaultRedisScript<Long> rotateCasScript;   // [B] 회전 스크립트

    private static final String JTI_PREFIX  = "rtjti:v1:";
    private static final String SESS_PREFIX = "rtsess:v1:";

    // --------- [A] JTI 원타임 ---------
    @Override
    public void saveJti(String jti, String userId, Duration ttl) {
        redis.opsForValue().set(JTI_PREFIX + jti, userId, ttl);
    }

    @Override
    public String consumeJtiOnce(String jti) {
        return redis.execute(consumeScript, Collections.singletonList(JTI_PREFIX + jti));
    }

    // --------- [B] RTR: userId 기반으로만 저장 ---------
    @Override
    public void putSessionJti(String userId, String jti, Duration ttl) {
        String key = sessionKey(userId); // sessionId 제거
        redis.opsForValue().set(key, jti, ttl);
    }

    @Override
    public long rotateSessionJtiIfMatches(String userId, String currentJti, String nextJti, Duration ttl) {
        String key = sessionKey(userId);
        Long result = redis.execute(
                rotateCasScript,
                Collections.singletonList(key),
                currentJti, nextJti, String.valueOf(ttl.getSeconds())
        );

        if (result == null) {
            return -2; // Redis 오류
        } else {
            return result; // 1:성공, 0:없음, -1:불일치
        }
    }

    @Override
    public void deleteSession(String userId) {
        redis.delete(sessionKey(userId)); // sessionId 제거
    }

    @Override
    public String getSessionJti(String userId) {
        return redis.opsForValue().get(sessionKey(userId));
    }

    private String sessionKey(String userId) {
        return SESS_PREFIX + userId;
    }
}
