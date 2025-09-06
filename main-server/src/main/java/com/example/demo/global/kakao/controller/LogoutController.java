package com.example.demo.global.kakao.controller;

import com.example.demo.global.http.util.AuthContextExtractor;
import com.example.demo.global.security.filter.CustomUserDetails;
import com.example.demo.global.kakao.service.LogoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "로그아웃 API")
public class LogoutController {

    private final LogoutService logoutService;
    private final AuthContextExtractor extractor;

    @Operation(
            summary = "로그아웃",
            description = """
                HttpOnly 쿠키 기반 세션을 종료합니다.
                
                - 클라이언트의 요청 쿠키(refresh, sessionId 등)를 기반으로 인증 정보를 추출합니다.
                - 서버 측 RefreshToken 상태도 만료 처리합니다.
                
                ⚠️ Swagger에서는 HttpOnly 쿠키를 자동 전송할 수 없기 때문에, 브라우저 환경에서 테스트하세요.
                """
    )
    @ApiResponse(responseCode = "200", description = "로그아웃 성공")
    @ApiResponse(responseCode = "400", description = "쿠키 누락 또는 유효하지 않음")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Parameter(hidden = true) HttpServletRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        var result = extractor.extract(request);
        if (!result.isOk()) {
            return ResponseEntity.badRequest().build();
        }

        logoutService.logout(customUserDetails.getId());
        return ResponseEntity.ok().build();
    }
}
