package com.example.demo.kakao.service;

import com.example.common.error.code.KakaoErrorCode;
import com.example.common.error.exception.UserException;
import com.example.demo.kakao.controller.KakaoApiHttp;
import com.example.demo.kakao.controller.KakaoAuthHttp;
import com.example.demo.kakao.dto.KakaoProfileResponse;
import com.example.demo.kakao.dto.KakaoTokenResponse;
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

    @Value("${deulak.kakao.redirect-uri}")
    private String redirectUri;

    /**
     * 인가코드 + PKCE(code_verifier)로 카카오 access_token 교환
     */
    public KakaoTokenResponse exchangeAuthorizationCode(String code, String codeVerifier) {
        requireText(code, "code");
        requireText(codeVerifier, "code_verifier");

        log.info("[KakaoAuth] 인가코드 수신: code={}, codeVerifier={}", code, codeVerifier);

        try {
            log.info("[KakaoAuth] 카카오 토큰 요청 시작");

            KakaoTokenResponse token = kakaoAuthHttp.token(
                    "authorization_code",
                    clientId,
                    code,
                    redirectUri,
                    codeVerifier
            );

            log.info("[KakaoAuth] 응답 성공! 받은 토큰: access_token={}, expires_in={}, refresh_token={}",
                    token.access_token(), token.expires_in(), token.refresh_token());

            return token;

        } catch (Exception e) {
            log.error("[KakaoAuth] 카카오 토큰 요청 중 예외 발생", e);
            throw new UserException("카카오 토큰 요청 실패", KakaoErrorCode.KAKAO_BAD_REQUEST);
        }
    }

    /**
     * access_token으로 사용자 프로필 조회
     */
    public KakaoProfileResponse getProfile(String accessToken) {
        requireText(accessToken, "access_token");

        log.info("[KakaoAuth] 사용자 프로필 요청: access_token={}", accessToken);

        try {
            KakaoProfileResponse profile = kakaoApiHttp.me("Bearer " + accessToken);
            log.info("[KakaoAuth] 사용자 프로필 응답: id={}, nickname={}",
                    profile.id(),
                    profile.kakao_account() != null && profile.kakao_account().profile() != null
                            ? profile.kakao_account().profile().nickname()
                            : "N/A");

            return profile;

        } catch (Exception e) {
            log.error("[KakaoAuth] 사용자 프로필 조회 중 예외 발생", e);
            throw new UserException("카카오 프로필 조회 실패", KakaoErrorCode.KAKAO_BAD_REQUEST);
        }
    }

    private static void requireText(String v, String name) {
        if (!StringUtils.hasText(v)) {
            throw new IllegalArgumentException("Required parameter missing: " + name);
        }
    }
}
