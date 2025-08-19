package com.example.demo.kakao.controller;

import com.example.demo.global.http.util.AuthContextExtractor;
import com.example.demo.global.security.filter.CustomUserDetails;
import com.example.demo.kakao.service.LogoutService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class LogoutController {

    private final LogoutService logoutService;
    private final AuthContextExtractor extractor;

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        var result = extractor.extract(request);
        if (!result.isOk()) {
            return ResponseEntity.badRequest().build(); // 쿠키 누락 등 실패 사유 있음
        }

        var ctx = result.context();
        logoutService.logout(customUserDetails.getUsername());
        return ResponseEntity.ok().build();
    }
}
