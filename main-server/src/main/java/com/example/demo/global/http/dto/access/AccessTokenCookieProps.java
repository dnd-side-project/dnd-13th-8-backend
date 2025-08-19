package com.example.demo.global.http.dto.access;

public record AccessTokenCookieProps(
        String name,
        String path,
        int ttlMinutes
) {}
