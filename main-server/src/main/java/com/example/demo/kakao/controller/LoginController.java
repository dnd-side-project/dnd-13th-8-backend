package com.example.demo.kakao.controller;

import com.example.demo.domain.user.entity.Users;
import com.example.demo.global.auth.refresh.store.RedisRefreshTokenStore;
import com.example.demo.global.jwt.JwtProps;
import com.example.demo.global.jwt.JwtProvider;
import com.example.demo.kakao.dto.KakaoLoginRequest;
import com.example.demo.kakao.dto.KakaoLoginResponse;
import com.example.demo.kakao.service.AuthService;
import com.example.demo.global.http.service.AccessTokenCookieService;
import com.example.demo.global.http.service.RefreshTokenCookieService;
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
    private final JwtProvider jwtProvider;
    private final JwtProps jwtProps;

    private final AccessTokenCookieService accessCookies;
    private final RefreshTokenCookieService refreshCookies;
    private final RedisRefreshTokenStore redisRefreshTokenStore;


    @PostMapping("/auth/login")
    public ResponseEntity<String> kakaoLogin(@Valid @RequestBody KakaoLoginRequest request) {
        // 1) 카카오 인증 후 우리 쪽 사용자 컨텍스트 획득
        KakaoLoginResponse out = authService.loginWithKakao(request.code(), request.codeVerifier());

        // 2) 새 Refresh 토큰 발급 (Access는 서비스에서 이미 생성해 돌려줬다고 가정: out.jwtAccessToken())
        String refresh = jwtProvider.issueRefresh(out.userId());
        String jti = jwtProvider.jti(refresh);

        // 3) Refresh 토큰 상태 저장소(예: Redis) 등록
        redisRefreshTokenStore.putSessionJti(
                out.userId(),
                jti,
                Duration.ofDays(jwtProps.refreshTtlDays())
        );

        // 4) Set-Cookie 헤더에 Access/Refresh 쿠키 내려주기
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookies.create(out.jwtAccessToken()).toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookies.create(refresh).toString())
                .body(out.userId());
    }


    @GetMapping("/auth/super")
    public ResponseEntity<String> superLogin () {
        Users users = new Users();
        String superToken = jwtProvider.issueAccess(users.getId());
        return ResponseEntity.ok().body(superToken);
    }
}
