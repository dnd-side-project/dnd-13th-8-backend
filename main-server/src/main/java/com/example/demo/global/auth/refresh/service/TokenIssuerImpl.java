package com.example.demo.global.auth.refresh.service;

import com.example.demo.global.auth.refresh.dto.IssuedTokens;
import com.example.demo.global.jwt.JwtProps;
import com.example.demo.global.jwt.JwtProvider;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenIssuerImpl implements TokenIssuer {

    private final JwtProvider jwt;
    private final JwtProps jwtProps;

    @Override
    public IssuedTokens preIssue(String userId) {
        String access  = jwt.issueAccess(userId);
        String refresh = jwt.issueRefresh(userId);
        String nextJti = jwt.jti(refresh);
        long   ttlSec  = Duration.ofDays(jwtProps.refreshTtlDays()).toSeconds();
        return new IssuedTokens(userId, access, refresh, nextJti, ttlSec);
    }
}
