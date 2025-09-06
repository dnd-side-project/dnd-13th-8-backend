package com.example.demo.global.kakao.service;

import com.example.demo.global.auth.refresh.store.RefreshTokenStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogoutService {

    private final RefreshTokenStore refreshTokenStore;

    /** 현재 세션 로그아웃 (세션 기반) */
    public void logout(String userId) {
        refreshTokenStore.deleteSession(userId);
    }
}
