package com.example.demo.global.kakao.service;

import com.example.common.error.code.UserErrorCode;
import com.example.common.error.exception.UserException;
import com.example.demo.domain.user.entity.Users;
import com.example.demo.domain.user.repository.UsersRepository;
import com.example.demo.domain.user.utils.ShareCodeGenerator;
import com.example.demo.global.jwt.JwtAccessIssuer;
import com.example.demo.global.kakao.dto.KakaoLoginResponse;
import com.example.demo.global.kakao.dto.KakaoProfileMapper;
import com.example.demo.global.kakao.dto.KakaoProfileResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
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
    public KakaoLoginResponse loginWithKakao(String code, String codeVerifier, String origin) {

        var token = kakaoAuthService.exchangeAuthorizationCode(code, codeVerifier, origin);

        KakaoProfileResponse profile = kakaoAuthService.getProfile(token.access_token());

        String kakaoId = kakaoProfileMapper.kakaoIdFrom(profile);

        var users = usersRepository.findByKakaoId(kakaoId)
                .orElseGet(() -> createUserWithShareCodeRetry(profile, kakaoId));

        String access = jwtAccessIssuer.issueUserToken(users.getId());

        return KakaoLoginResponse.from(users, access);
    }

    private Users createUserWithShareCodeRetry(KakaoProfileResponse profile, String kakaoId) {

        for (int i = 0; i < 10; i++) {
            try {
                String shareCode = ShareCodeGenerator.generate();
                var newUser = kakaoProfileMapper.newUserFromProfile(profile, shareCode);
                return usersRepository.save(newUser);

            } catch (DataIntegrityViolationException e) {

                // kakaoId가 이미 생성된 경우 (동시 로그인)
                var existing = usersRepository.findByKakaoId(kakaoId);
                if (existing.isPresent()) return existing.get();

                // shareCode 충돌이면 그냥 재시도
            }
        }

        throw new UserException(UserErrorCode.DUPLICATED_SHARECODE);
    }
}
