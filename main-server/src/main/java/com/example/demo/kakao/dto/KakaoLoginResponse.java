package com.example.demo.kakao.dto;


public record KakaoLoginResponse(
        String userId,
        String jwtAccessToken
) {}

