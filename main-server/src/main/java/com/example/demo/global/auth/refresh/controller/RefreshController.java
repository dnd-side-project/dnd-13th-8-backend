package com.example.demo.global.auth.refresh.controller;

import com.example.demo.global.auth.refresh.dto.ApiResponse;
import com.example.demo.global.auth.refresh.dto.RefreshFailure;
import com.example.demo.global.auth.refresh.dto.RefreshResult;
import com.example.demo.global.auth.refresh.dto.RefreshSuccess;
import com.example.demo.global.auth.refresh.service.RefreshService;
import com.example.demo.global.http.util.AuthContextExtractor;
import com.example.demo.global.http.dto.auth.AuthContextResult;
import com.example.demo.global.http.service.AccessTokenCookieService;
import com.example.demo.global.http.service.RefreshTokenCookieService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class RefreshController {

    private final RefreshService refreshService;
    private final AuthContextExtractor extractor;
    private final AccessTokenCookieService accessCookies;
    private final RefreshTokenCookieService refreshCookies;

    @PostMapping("/auth/refresh")
    public ResponseEntity<ApiResponse<Void>> refresh(HttpServletRequest req) {
        // 1) 요청 컨텍스트 추출 (refresh / sessionId / userId)
        AuthContextResult ctx = extractor.extract(req);
        if (!ctx.isOk()) {
            // 필요하면 실패 사유를 헤더에 노출 (X-Refresh-Failure-Reason 등)
            return unauthorizedClear(null);
        }

        // 2) 서비스 호출
        var c = ctx.context();
        RefreshResult result = refreshService.refresh(c.refreshJwt());

        // 3) 성공/실패 분기
        if (result instanceof RefreshSuccess s) {
            var tokens = s.getTokens();
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, accessCookies.create(tokens.accessToken()).toString())
                    .header(HttpHeaders.SET_COOKIE, refreshCookies.create(tokens.refreshToken()).toString())
                    .body(ApiResponse.ok(null));
        }

        var reason = (result instanceof RefreshFailure f) ? f.getReason() : null;
        return unauthorizedClear(reason);
    }

    private ResponseEntity<ApiResponse<Void>> unauthorizedClear(RefreshFailure.Reason reason) {
        var builder = ResponseEntity.status(401)
                .header(HttpHeaders.SET_COOKIE, accessCookies.clear().toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookies.clear().toString());
        if (reason != null) {
            builder.header("X-Refresh-Failure-Reason", reason.name());
        }
        return builder.body(ApiResponse.error("Refresh token invalid or expired"));
    }
}
