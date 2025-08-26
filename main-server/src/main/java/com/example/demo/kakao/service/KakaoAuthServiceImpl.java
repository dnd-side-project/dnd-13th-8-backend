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

        var token = kakaoAuthService.exchangeAuthorizationCode(code, codeVerifier);

        KakaoProfileResponse profile = kakaoAuthService.getProfile(token.access_token());

        String kakaoId = kakaoProfileMapper.kakaoIdFrom(profile);

        var users = usersRepository.findByKakaoId(kakaoId)
                .orElseGet(() -> {
                    var newUser = kakaoProfileMapper.newUserFromProfile(profile);
                    return usersRepository.save(newUser);
                });
        String access = jwtAccessIssuer.issueUserToken(users.getId());

        return new KakaoLoginResponse(users.getId(), users.getUsername(), access);
    }
}
