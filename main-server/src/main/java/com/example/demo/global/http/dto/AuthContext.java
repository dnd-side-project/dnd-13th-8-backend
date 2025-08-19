package com.example.demo.global.http.dto;

public record AuthContext(
        String userId,
        String sessionId,
        String refreshJwt
) {}