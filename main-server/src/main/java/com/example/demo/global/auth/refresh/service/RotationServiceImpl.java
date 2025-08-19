package com.example.demo.global.auth.refresh.service;

import com.example.demo.global.auth.refresh.store.RefreshTokenStore;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RotationServiceImpl implements RotationService {
    private final RefreshTokenStore store; // Redis CAS 호출 래핑

    @Override
    public long rotate(String userId, String sessionId, String presentedJti, String nextJti, long ttlSeconds) {
        return store.rotateSessionJtiIfMatches(
                userId, sessionId, presentedJti, nextJti, Duration.ofSeconds(ttlSeconds)
        );
    }
}
