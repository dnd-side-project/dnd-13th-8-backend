package com.example.demo.global.auth.refresh.dto;


/**
 * Refresh 토큰 재발급 실패 결과 DTO
 */
public final class RefreshFailure implements RefreshResult {

    public enum Reason {
        NOT_FOUND,   // 세션 키 없음/만료
        MISMATCH,    // 현재 jti 불일치(재사용 의심)
        INVALID,     // JWT 서명/구조/만료 등 검증 실패
        ERROR        // 기타 실행 오류
    }

    private final Reason reason;

    public RefreshFailure(Reason reason) {
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }
}
