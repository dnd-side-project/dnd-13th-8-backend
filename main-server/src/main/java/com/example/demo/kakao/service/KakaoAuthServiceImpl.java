package com.example.demo.kakao.service;

import com.example.demo.domain.user.repository.UsersRepository;
import com.example.demo.global.jwt.JwtAccessIssuer;
import com.example.demo.kakao.dto.KakaoLoginResponse;
import com.example.demo.kakao.dto.KakaoProfileMapper;
import com.example.demo.kakao.dto.KakaoProfileResponse;
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

        log.info("[Kakao Login] 인가 코드 수신: code={}, codeVerifier={}", code, codeVerifier);

        var token = kakaoAuthService.exchangeAuthorizationCode(code, codeVerifier);
        log.info("[Kakao Login] 액세스 토큰 수신: access_token={}", token.access_token());

        KakaoProfileResponse profile = kakaoAuthService.getProfile(token.access_token());
        log.info("[Kakao Login] 카카오 프로필 조회 완료: id={}, nickname={}",
                profile.id(),
                profile.kakao_account() != null && profile.kakao_account().profile() != null
                        ? profile.kakao_account().profile().nickname()
                        : "N/A");

        String kakaoId = kakaoProfileMapper.kakaoIdFrom(profile);
        log.info("[Kakao Login] 파싱된 kakaoId={}", kakaoId);

        var users = usersRepository.findByKakaoId(kakaoId)
                .orElseGet(() -> {
                    log.info("[Kakao Login] 기존 사용자 없음 → 신규 사용자 생성 진행");
                    var newUser = kakaoProfileMapper.newUserFromProfile(profile);
                    var savedUser = usersRepository.save(newUser);
                    log.info("[Kakao Login] 신규 사용자 생성 완료: userId={}, username={}", savedUser.getId(), savedUser.getUsername());
                    return savedUser;
                });

        String access = jwtAccessIssuer.issueUserToken(users.getId());
        log.info("[Kakao Login] JWT 액세스 토큰 발급 완료: userId={}", users.getId());

        return new KakaoLoginResponse(users.getId(), users.getUsername(), users.getProfileUrl(), access);
    }
}
