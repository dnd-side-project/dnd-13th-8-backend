package com.example.demo.global.auth.refresh.store;

import java.time.Duration;

/**
 * Refresh Token 저장소 인터페이스
 * - Redis 기반 구현체에서 원자적 I/O 프리미티브 제공
 * - 비즈니스 로직(회전/검증/차단)은 Service에서 구현
 */
public interface RefreshTokenStore {

    // [A] JTI 기반 원타임 토큰 저장
    void saveJti(String jti, String userId, Duration ttl);

    // [A] JTI 기반 원타임 토큰 소비 (있으면 userId 반환 후 즉시 삭제)
    String consumeJtiOnce(String jti);

    // [B] 세션 기반 현재 jti 저장
    void putSessionJti(String userId, String jti, Duration ttl);

    // [B] 세션 기반 CAS 회전 (결과: 1=성공, 0=없음, -1=불일치, -2=에러)
    long rotateSessionJtiIfMatches(String userId, String currentJti, String nextJti, Duration ttl);

    // 세션 삭제
    void deleteSession(String userId);

    // 세션 현재 jti 조회
    String getSessionJti(String userId);
}
