package com.example.demo.global.jwt;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtAccessIssuer {

    private final JwtProvider jwtProvider;

    /** 일반 회원용 access 토큰 (30분) */
    public String issueUserToken(String userId) {
        jwtProvider.validateSubject(userId);
        return jwtProvider.buildToken(userId, 30 * 60L, Map.of(
                "typ", "access",
                "role", JwtRoleType.USER.value()
        ));
    }

    /** 슈퍼 계정용 access 토큰 (30일) */
    public String issueSuperToken(String userId) {
        jwtProvider.validateSubject(userId);
        return jwtProvider.buildToken(userId, 30L * 24 * 60 * 60, Map.of(
                "typ", "access",
                "role", JwtRoleType.SUPER.value()
        ));
    }

    /** 익명 계정용 access 토큰 (12시간) */
    public String issueAnonymousToken(String tempId) {
        jwtProvider.validateSubject(tempId);
        return jwtProvider.buildToken(tempId, 12 * 60 * 60L, Map.of(
                "typ", "access",
                "role", JwtRoleType.ANONYMOUS.value()
        ));
    }

}
