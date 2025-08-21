package com.example.demo.global.auth.refresh.controller;

import com.example.demo.global.auth.refresh.dto.ApiResponse;
import com.example.demo.global.auth.refresh.dto.RefreshFailure;
import com.example.demo.global.auth.refresh.dto.RefreshResult;
import com.example.demo.global.auth.refresh.dto.RefreshSuccess;
import com.example.demo.global.auth.refresh.service.RefreshService;
import com.example.demo.global.http.dto.auth.AuthContextResult;
import com.example.demo.global.http.service.AccessTokenCookieService;
import com.example.demo.global.http.service.RefreshTokenCookieService;
import com.example.demo.global.http.util.AuthContextExtractor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "토큰 재발급 (Refresh) API")
public class RefreshController {

    private final RefreshService refreshService;
    private final AuthContextExtractor extractor;
    private final AccessTokenCookieService accessCookies;
    private final RefreshTokenCookieService refreshCookies;

    @Operation(
            summary = "AccessToken 재발급",
            description = """
                HttpOnly 쿠키에서 RefreshToken, SessionId, UserId를 추출하여 새 토큰을 발급합니다.
                
                - 성공 시 새로운 AccessToken + RefreshToken을 쿠키로 내려줍니다.
                - 실패 시 기존 쿠키 제거 + 401 반환 + 실패 사유 헤더(`X-Refresh-Failure-Reason`) 포함.
                
                ⚠️ Swagger에서는 HttpOnly 쿠키 테스트가 불가하므로 Postman 또는 실제 클라이언트에서 호출하세요.
                """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "재발급 성공 (Set-Cookie로 토큰 전송)")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "재발급 실패 (토큰 만료/세션 불일치)",
            headers = @Header(name = "X-Refresh-Failure-Reason", description = "실패 사유 코드")
    )
    @PostMapping("/auth/refresh")
    public ResponseEntity<ApiResponse<Void>> refresh(HttpServletRequest req) {
        AuthContextResult ctx = extractor.extract(req);
        if (!ctx.isOk()) {
            return unauthorizedClear(null);
        }

        var c = ctx.context();
        RefreshResult result = refreshService.refresh(c.refreshJwt());

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
