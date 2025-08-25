package com.example.demo.global.auth.refresh.service;

import com.example.demo.global.auth.refresh.dto.IssuedTokens;
import com.example.demo.global.jwt.JwtAccessIssuer;
import com.example.demo.global.jwt.JwtProps;
import com.example.demo.global.jwt.JwtProvider;
import com.example.demo.global.jwt.JwtRefreshIssuer;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenIssuerImpl implements TokenIssuer {

    private final JwtAccessIssuer jwtAccessIssuer;
    private final JwtRefreshIssuer jwtRefreshIssuer;
    private final JwtProvider jwtProvider;
    private final JwtProps jwtProps;

    @Override
    public IssuedTokens preIssue(String userId) {
        String access  = jwtAccessIssuer.issueUserToken(userId);
        String refresh = jwtRefreshIssuer.issueRefresh(userId);
        String nextJti = jwtProvider.jti(refresh);
        long   ttlSec  = Duration.ofDays(jwtProps.refreshTtlDays()).toSeconds();
        return new IssuedTokens(userId, access, refresh, nextJti, ttlSec);
    }
}
