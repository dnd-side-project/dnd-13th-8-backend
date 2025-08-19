package com.example.demo.global.http.dto;

public record AccessTokenCookieProps(
        String name,
        String path,
        int ttlMinutes
) {}
