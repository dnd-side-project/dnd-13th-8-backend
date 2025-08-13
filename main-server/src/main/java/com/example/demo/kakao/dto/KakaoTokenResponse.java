package com.example.demo.kakao.dto;


public record KakaoTokenResponse(
        String access_token,
        String token_type,
        String refresh_token,
        Long expires_in,
        Long refresh_token_expires_in,
        String scope
) {}
