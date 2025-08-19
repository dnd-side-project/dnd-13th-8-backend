package com.example.demo.global.http.dto;

import com.example.demo.global.http.dto.access.AccessTokenCookieProps;
import com.example.demo.global.http.dto.refresh.RefreshTokenCookieProps;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "deulak.cookie")
public record CookieProps(
        AccessTokenCookieProps access,
        RefreshTokenCookieProps refresh,
        SessionCookieProps session,
        CommonCookieProps common
) {}
