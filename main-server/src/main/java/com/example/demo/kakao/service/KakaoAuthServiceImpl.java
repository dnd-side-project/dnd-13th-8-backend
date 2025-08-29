package com.example.demo.kakao.service;

import com.example.demo.domain.user.repository.UsersRepository;
import com.example.demo.global.jwt.JwtAccessIssuer;
import com.example.demo.kakao.dto.KakaoLoginResponse;
import com.example.demo.kakao.dto.KakaoProfileMapper;
import com.example.demo.kakao.dto.KakaoProfileResponse;
import com.example.demo.kakao.service.AuthService;
import com.example.demo.kakao.service.KakaoAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoAuthServiceImpl implements AuthService {

    private final KakaoAuthService kakaoAuthService;
    private final KakaoProfileMapper kakaoProfileMapper;
    private final JwtAccessIssuer jwtAccessIssuer;
    private final UsersRepository usersRepository;

    @Override
    public KakaoLoginResponse loginWithKakao(String code, String codeVerifier) {
        log.info("[카카오 로그인] 인가코드 수신: code={}, codeVerifier={}", code, codeVerifier);

        var token = kakaoAuthService.exchangeAuthorizationCode(code, codeVerifier);
        log.info("[카카오 로그인] Access Token 발급 완료: {}", token.access_token());

        KakaoProfileResponse profile = kakaoAuthService.getProfile(token.access_token());
        log.info("[카카오 로그인] 프로필 조회 완료: id={}, nickname={}",
                profile.id(),
                profile.kakao_account() != null && profile.kakao_account().profile() != null
                        ? profile.kakao_account().profile().nickname()
                        : "N/A"
        );

        String kakaoId = kakaoProfileMapper.kakaoIdFrom(profile);
        log.info("[카카오 로그인] kakaoId 추출: {}", kakaoId);

        var users = usersRepository.findByKakaoId(kakaoId)
                .orElseGet(() -> {
                    var newUser = kakaoProfileMapper.newUserFromProfile(profile);
                    log.info("[카카오 로그인] 신규 사용자 생성: nickname={}, profileUrl={}",
                            newUser.getUsername(), newUser.getProfileUrl());
                    return usersRepository.save(newUser);
                });

        String access = jwtAccessIssuer.issueUserToken(users.getId());
        log.info("[카카오 로그인] JWT 발급 완료: userId={}, username={}", users.getId(), users.getUsername());

        return new KakaoLoginResponse(users.getId(), users.getUsername(), users.getProfileUrl(), access);
    }
}
