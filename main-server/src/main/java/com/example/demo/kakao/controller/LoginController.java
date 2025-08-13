package com.example.demo.kakao.controller;

import com.example.demo.global.http.HttpOnlyCookieUtil;
import com.example.demo.kakao.dto.KakaoLoginRequest;
import com.example.demo.kakao.dto.KakaoLoginResponse;
import com.example.demo.kakao.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor
public class LoginController {

    private final AuthService authService;
    private final HttpOnlyCookieUtil cookieUtil;

    @PostMapping("/auth/login")
    public ResponseEntity<String> kakaoLogin(@Valid @RequestBody KakaoLoginRequest request) {
        KakaoLoginResponse out = authService.loginWithKakao(request.code(), request.codeVerifier());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieUtil.accessCookie(out.jwtAccessToken()).toString())
                .body(out.username());
    }
}
