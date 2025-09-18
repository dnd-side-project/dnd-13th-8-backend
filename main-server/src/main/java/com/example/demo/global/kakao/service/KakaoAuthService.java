package com.example.demo.global.kakao.service;

import com.example.demo.global.kakao.dto.KakaoProfileResponse;
import com.example.demo.global.kakao.dto.KakaoTokenResponse;
import com.example.demo.global.kakao.controller.KakaoApiHttp;
import com.example.demo.global.kakao.controller.KakaoAuthHttp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoAuthService {

    private final KakaoAuthHttp kakaoAuthHttp;
    private final KakaoApiHttp kakaoApiHttp;

    @Value("${deulak.kakao.client-id}")
    private String clientId;

    @Value("${deulak.kakao.local-redirect-uri}")
    private String localRedirectUri;

    @Value("${deulak.kakao.dev-redirect-uri}")
    private String devRedirectUri;

    @Value("${deulak.kakao.prod-redirect-uri}")
    private String prodRedirectUri;

    /**
     * 인가코드 + PKCE(code_verifier)로 카카오 access_token 교환
     */
    public KakaoTokenResponse exchangeAuthorizationCode(String code, String codeVerifier, String origin) {
        requireText(code, "code");
        requireText(codeVerifier, "code_verifier");

        String redirectUri;
        if (origin.equals(localRedirectUri)) {
            redirectUri = localRedirectUri + "/login/callback";
        } else if (origin.equals(devRedirectUri)) {
            redirectUri = devRedirectUri + "/login/callback";
        } else if (origin.equals(prodRedirectUri)) {
            redirectUri = prodRedirectUri + "/login/callback";
        } else {
            redirectUri = devRedirectUri + "/login/callback"; // 기본값으로 운영 사용도 가능
        }

        var token = kakaoAuthHttp.token(
                "authorization_code",
                clientId,
                code,
                redirectUri,       //
                codeVerifier
        );


        log.info("[KakaoAuth] 카카오 토큰 응답 수신: access_token={}, expires_in={}s, refresh_token={}",
                token.access_token(), token.expires_in(), token.refresh_token());

        return token;
    }

    /**
     * access_token으로 사용자 프로필 조회
     */
    public KakaoProfileResponse getProfile(String accessToken) {
        requireText(accessToken, "access_token");
        return kakaoApiHttp.me("Bearer " + accessToken);
    }

    private static void requireText(String v, String name) {
        if (!StringUtils.hasText(v)) {
            throw new IllegalArgumentException("Required parameter missing: " + name);
        }
    }
}
