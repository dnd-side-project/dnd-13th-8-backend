package com.example.demo.kakao.service;

import com.example.demo.global.jwt.JwtAccessIssuer;
import com.example.demo.global.jwt.JwtProvider;
import com.example.demo.kakao.dto.KakaoLoginResponse;
import com.example.demo.kakao.dto.KakaoProfileMapper;
import com.example.demo.kakao.dto.KakaoProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KakaoAuthServiceImpl implements AuthService {

    private final KakaoAuthService kakaoAuthService; // 카카오 토큰교환/프로필 조회
    private final KakaoProfileMapper kakaoProfileMapper;
    private final JwtAccessIssuer jwtAccessIssuer;


    @Override
    public KakaoLoginResponse loginWithKakao(String code, String codeVerifier) {
        var token = kakaoAuthService.exchangeAuthorizationCode(code, codeVerifier);
        KakaoProfileResponse profile = kakaoAuthService.getProfile(token.access_token());

        var users = kakaoProfileMapper.newUserFromProfile(profile);;

        String access = jwtAccessIssuer.issueUserToken(users.getId());

        return new KakaoLoginResponse(users.getId(), users.getUsername(), access);
    }

}
