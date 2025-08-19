package com.example.demo.global.auth.refresh.dto;

/**
 * Refresh 토큰 재발급 성공 결과 DTO
 */
public final class RefreshSuccess implements RefreshResult {

    private final TokenPair tokens;

    public RefreshSuccess(TokenPair tokens) {
        this.tokens = tokens;
    }

    public TokenPair getTokens() {
        return tokens;
    }
}
