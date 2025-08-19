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
    private final DefaultRedisScript<Long> rotateCasScript;   // [B] 세션 회전 스크립트

    // Redis 키 prefix (버전명 포함: 마이그레이션 대비)
    private static final String JTI_PREFIX  = "rtjti:v1:";
    private static final String SESS_PREFIX = "rtsess:v1:";

    // --------- [A] JTI 원타임 ---------
    @Override
    public void saveJti(String jti, String userId, Duration ttl) {
        redis.opsForValue().set(JTI_PREFIX + jti, userId, ttl);
    }

    @Override
    public String consumeJtiOnce(String jti) {
        // Lua 스크립트 실행 → GET + DEL 원자화
        return redis.execute(consumeScript, Collections.singletonList(JTI_PREFIX + jti));
        // Redis 6.2+ : return redis.opsForValue().getAndDelete(JTI_PREFIX + jti);
    }

    // --------- [B] 세션 기반 RTR ---------
    @Override
    public void putSessionJti(String userId, String sessionId, String jti, Duration ttl) {
        String key = sessionKey(userId, sessionId);
        redis.opsForValue().set(key, jti, ttl);
    }

    @Override
    public long rotateSessionJtiIfMatches(String userId, String sessionId, String currentJti, String nextJti, Duration ttl) {
        String key = sessionKey(userId, sessionId);
        Long result = redis.execute(
                rotateCasScript,
                Collections.singletonList(key),
                currentJti, nextJti, String.valueOf(ttl.getSeconds())
        );

        if (result == null) {
            return -2; // Redis 오류
        }
        return result; // 1:성공, 0:없음, -1:불일치
    }

    @Override
    public void deleteSession(String userId, String sessionId) {
        redis.delete(sessionKey(userId, sessionId));
    }

    @Override
    public String getSessionJti(String userId, String sessionId) {
        return redis.opsForValue().get(sessionKey(userId, sessionId));
    }

    // 세션 키 생성
    private String sessionKey(String userId, String sessionId) {
        return SESS_PREFIX + userId + ":" + sessionId;
    }
}
