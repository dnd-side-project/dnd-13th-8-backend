package com.example.demo.global.http.dto.auth;

public record AuthContext(
        String userId,
        String sessionId,
        String refreshJwt
) {}