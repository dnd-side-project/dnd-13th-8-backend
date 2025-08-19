package com.example.demo.global.http.dto;

public record RefreshTokenCookieProps(
        String name,
        String path,
        Long ttlDays
) {}
