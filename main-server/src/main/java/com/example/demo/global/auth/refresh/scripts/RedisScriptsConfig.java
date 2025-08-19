package com.example.demo.global.auth.refresh.scripts;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.script.DefaultRedisScript;

/**
 * Redis Lua 스크립트들을 Spring Bean으로 등록하는 설정 클래스
 * - Store(Repository)에서는 이 Bean들을 주입받아 사용
 * - 목적: 스크립트를 한 곳에서 관리하고 재사용성 및 테스트 편의성 확보
 */
@Configuration
public class RedisScriptsConfig {

    /**
     * [A] JTI 원타임 소비 스크립트 (GET + DEL 원자화)
     * - KEYS[1] : rtjti:v1:{jti}
     * - 값이 있으면 반환하고, 즉시 삭제
     * - 없으면 nil 반환
     * - 재사용 방지(1회성) 목적
     */
    @Bean
    public DefaultRedisScript<String> consumeScript() {
        var script = new DefaultRedisScript<String>();
        script.setResultType(String.class);
        script.setScriptText("""
            local v = redis.call('GET', KEYS[1])
            if v then redis.call('DEL', KEYS[1]) end
            return v
        """);
        return script;
    }

    /**
     * [B] 세션 기반 RTR 회전(CAS: Compare-And-Set) 스크립트
     * - KEYS[1] : rtsess:v1:{userId}:{sessionId}
     * - ARGV[1] : 현재 제출된 jti
     * - ARGV[2] : 교체할 새로운 jti
     * - ARGV[3] : TTL(초)
     * 동작:
     *   1) 현재 값 조회
     *   2) 없으면 0 반환 (만료/삭제)
     *   3) 값 불일치면 -1 반환 (재사용 의심)
     *   4) 값 일치 시 -> 새 jti로 교체 + TTL 재설정 -> 1 반환
     */
    @Bean
    public DefaultRedisScript<Long> rotateCasScript() {
        var script = new DefaultRedisScript<Long>();
        script.setResultType(Long.class);
        script.setScriptText("""
            local cur = redis.call('GET', KEYS[1])
            if not cur then return 0 end
            if cur ~= ARGV[1] then return -1 end
            redis.call('SET', KEYS[1], ARGV[2])
            redis.call('EXPIRE', KEYS[1], tonumber(ARGV[3]))
            return 1
        """);
        return script;
    }
}
