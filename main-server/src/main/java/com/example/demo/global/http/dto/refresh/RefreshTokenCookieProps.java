package com.example.demo.global.http.dto.refresh;

public record RefreshTokenCookieProps(
        String name,
        String path,
        Long ttlDays
) {

}
