package com.example.demo.kakao.service;

import com.example.demo.kakao.dto.KakaoProfileResponse;
import com.example.demo.kakao.dto.KakaoTokenResponse;
import com.example.demo.kakao.controller.KakaoApiHttp;
import com.example.demo.kakao.controller.KakaoAuthHttp;
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

    @Value("${dulak.kakao.client-id}")
    private String clientId;

    @Value("${dulak.kakao.redirect-uri}")
    private String redirectUri;

    /**
     * 인가코드 + PKCE(code_verifier)로 카카오 access_token 교환
     */
    public KakaoTokenResponse exchangeAuthorizationCode(String code, String codeVerifier) {
        requireText(code, "code");
        requireText(codeVerifier, "code_verifier");
        var token = kakaoAuthHttp.token(
                "authorization_code",
                clientId,
                code,
                codeVerifier
        );
        log.debug("Kakao token issued (expiresIn={}s)", token.expires_in());
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
