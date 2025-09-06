package com.example.demo.global.kakao.service;

import com.example.demo.global.kakao.dto.KakaoLoginResponse;

public interface AuthService {
    KakaoLoginResponse loginWithKakao(String code, String codeVerifier);
}

