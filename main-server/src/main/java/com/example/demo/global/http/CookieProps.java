package com.example.demo.global.http;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "dulak.cookie")
public record CookieProps(
        String domain,
        String accessName,
        String accessPath,
        int accessTtlMinutes,
        String sameSite,   // "Lax" | "None" | "Strict"
        boolean secure,
        boolean httpOnly
) {}
