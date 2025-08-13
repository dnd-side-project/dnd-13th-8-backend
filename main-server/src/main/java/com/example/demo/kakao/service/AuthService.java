package com.example.demo.kakao.service;

import com.example.demo.kakao.dto.KakaoLoginResponse;

public interface AuthService {
    KakaoLoginResponse loginWithKakao(String code, String codeVerifier);
}

