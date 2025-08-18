package com.example.demo.global.http;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "deulak.cookie")
public record CookieProps(
        String domain,
        String accessName,
        String accessPath,
        String refreshPath,
        String refreshName,
        int accessTtlMinutes,
        Long refreshTtlDays,
        String sameSite,   // "Lax" | "None" | "Strict"
        boolean secure,
        boolean httpOnly
) {
}
