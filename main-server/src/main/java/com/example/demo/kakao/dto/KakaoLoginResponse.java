package com.example.demo.kakao.dto;


public record KakaoLoginResponse(
        String username,
        String jwtAccessToken
) {}

