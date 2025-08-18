package com.example.demo.kakao.controller;

import com.example.demo.global.http.HttpOnlyCookieUtil;
import com.example.demo.global.jwt.JwtProps;
import com.example.demo.global.jwt.JwtProvider;
import com.example.demo.kakao.dto.KakaoLoginRequest;
import com.example.demo.kakao.dto.KakaoLoginResponse;
import com.example.demo.kakao.service.AuthService;
import java.time.Duration;
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
    private final JwtProvider jwtProvider;
    private final JwtProps jwtProps;

    /*
    @PostMapping("/auth/login")
    public ResponseEntity<String> kakaoLogin(@Valid @RequestBody KakaoLoginRequest request) {
        KakaoLoginResponse out = authService.loginWithKakao(request.code(), request.codeVerifier());

        // 새 Refresh 발급
        String refresh = jwtProvider.issueRefresh(out.userId());
        String jti = jwtProvider.jti(refresh);
        //refreshTokenStore.store(jti, out.userId(), Duration.ofDays(jwtProps.refreshTtlDays()));

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieUtil.accessCookie(out.jwtAccessToken()).toString())
                .header(HttpHeaders.SET_COOKIE, cookieUtil.refreshCookie(refresh).toString())
                .body(out.userId());
    }
     */

    @GetMapping("/auth/super")
    public ResponseEntity<String> superLogin () {
        String superToken = jwtProvider.issueAccess("test");
        return ResponseEntity.ok().body(superToken);
    }
}
