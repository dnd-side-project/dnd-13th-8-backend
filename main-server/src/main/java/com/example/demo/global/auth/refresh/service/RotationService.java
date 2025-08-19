package com.example.demo.global.auth.refresh.service;

public interface RotationService {
    /** @return 1=성공, 0=세션없음/만료, -1=불일치(재사용의심), -2=실행오류 */
    long rotate(String userId, String sessionId, String presentedJti, String nextJti, long ttlSeconds);
}