package com.example.demo.global.auth.refresh.service;

import com.example.demo.global.kakao.service.LogoutService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogoutOnReuseHandler implements ReuseHandler {

    private final LogoutService logoutService;

    @Override
    public void onSuspiciousReuse(String userId) {
        logoutService.logout(userId);

        log.warn("❗ Refresh 재사용 탐지 → 해당 세션 로그아웃 처리 완료 (userId={})",
                userId);
    }
}



