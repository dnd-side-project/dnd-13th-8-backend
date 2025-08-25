package com.example.demo.kakao.controller;

import com.example.demo.domain.user.entity.Users;
import com.example.demo.domain.user.repository.UsersRepository;
import com.example.demo.global.auth.refresh.store.RedisRefreshTokenStore;
import com.example.demo.global.http.service.AccessTokenCookieService;
import com.example.demo.global.http.service.RefreshTokenCookieService;
import com.example.demo.global.jwt.JwtProps;
import com.example.demo.global.jwt.JwtProvider;
import com.example.demo.kakao.dto.KakaoLoginRequest;
import com.example.demo.kakao.dto.KakaoLoginResponse;
import com.example.demo.kakao.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Auth", description = "소셜 로그인 API (카카오)")
public class LoginController {

    private final AuthService authService;
    private final JwtProvider jwtProvider;
    private final JwtProps jwtProps;
    private final RefreshTokenCookieService refreshCookies;
    private final RedisRefreshTokenStore redisRefreshTokenStore;
    private final UsersRepository usersRepository;

    @PostMapping("/auth/login")
    public ResponseEntity<KakaoLoginResponse> kakaoLogin(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(schema = @Schema(implementation = KakaoLoginRequest.class))
            )
            @Valid @RequestBody KakaoLoginRequest request
    ) {
        KakaoLoginResponse out = authService.loginWithKakao(request.code(), request.codeVerifier());
        String refresh = jwtProvider.issueRefresh(out.userId());
        String jti = jwtProvider.jti(refresh);

        redisRefreshTokenStore.putSessionJti(
                out.userId(),
                jti,
                Duration.ofDays(jwtProps.refreshTtlDays())
        );

        return ResponseEntity.ok()
                // AccessToken은 쿠키 대신 body로 전달
                .header(HttpHeaders.SET_COOKIE, refreshCookies.create(refresh).toString())
                .body(out);
    }


    @Operation(summary = "슈퍼 로그인 (임시 개발용)", description = "슈퍼 계정용 Access 토큰 발급 (test, test2, test3...)")
    @ApiResponse(responseCode = "200", description = "슈퍼 토큰 발급 성공")
    @GetMapping("/auth/super")
    public ResponseEntity<String> superLogin() {
        Users user = new Users();
        user.setUsername("슈퍼테스트");
        Users savedUser = usersRepository.save(user);
        String superToken = jwtProvider.issueAccess(savedUser.getId());
        return ResponseEntity.ok().body(superToken);
    }

}
