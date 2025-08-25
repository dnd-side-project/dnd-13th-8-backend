package com.example.demo.global.jwt;

import com.example.common.error.code.JwtErrorCode;
import com.example.common.error.exception.JwtException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtRefreshIssuer {

    private final JwtProvider jwtProvider;
    private final int ttlDays = 7;

    /** Refresh 토큰 발급 (subject = userId) */
    public String issueRefresh(String userId) {
        jwtProvider.validateSubject(userId);
        if (ttlDays < 1) {
            throw new JwtException("Refresh 토큰 만료 시간이 유효하지 않습니다.", JwtErrorCode.JWT_INVALID);
        }
        return jwtProvider.buildToken(userId, ttlDays * 24L * 3600L, Map.of("typ", "refresh"));
    }
}
