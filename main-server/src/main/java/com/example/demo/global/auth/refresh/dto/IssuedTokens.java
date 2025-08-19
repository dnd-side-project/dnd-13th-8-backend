package com.example.demo.global.auth.refresh.dto;

public record IssuedTokens(
        String userId,
        String accessToken,
        String refreshToken,
        String nextJti,
        long refreshTtlSeconds
) {}
