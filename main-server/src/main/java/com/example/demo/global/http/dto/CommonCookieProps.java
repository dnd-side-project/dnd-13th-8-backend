package com.example.demo.global.http.dto;

public record CommonCookieProps(
        String domain,
        String sameSite,
        boolean secure,
        boolean httpOnly
) {}
