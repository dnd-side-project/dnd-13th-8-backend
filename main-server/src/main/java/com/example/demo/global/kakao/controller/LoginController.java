package com.example.demo.global.kakao.controller;

import com.example.demo.domain.user.entity.Users;
import com.example.demo.domain.user.repository.UsersRepository;
import com.example.demo.domain.user.service.NicknameGenerator;
import com.example.demo.global.jwt.JwtAccessIssuer;
import com.example.demo.global.jwt.JwtRoleType;
import com.example.demo.global.kakao.dto.KakaoLoginRequest;
import com.example.demo.global.kakao.dto.KakaoLoginResponse;
import com.example.demo.global.kakao.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Auth", description = "소셜 로그인 API (카카오)")
public class LoginController {

    private final AuthService authService;
    private final UsersRepository usersRepository;
    private final JwtAccessIssuer jwtAccessIssuer;
    private final NicknameGenerator nicknameGenerator;

    @PostMapping("/auth/login")
    public ResponseEntity<KakaoLoginResponse> kakaoLogin(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(schema = @Schema(implementation = KakaoLoginRequest.class))
            )
            @Valid @RequestBody KakaoLoginRequest request
    ) {
        KakaoLoginResponse out = authService.loginWithKakao(request.code(), request.codeVerifier());

        return ResponseEntity.ok().body(out);
    }

    @Operation(summary = "슈퍼 로그인 (임시 개발용)", description = "슈퍼 계정용 Access 토큰 발급 (test, test2, test3...)")
    @ApiResponse(responseCode = "200", description = "슈퍼 토큰 발급 성공")
    @GetMapping("/auth/super")
    public ResponseEntity<String> superLogin() {
        String nickname = nicknameGenerator.generateUniqueNickname();
        Users user = new Users();
        user.setUsername(nickname);
        user.setRole(JwtRoleType.SUPER);
        user.setProfileUrl("NULL");
        Users savedUser = usersRepository.save(user);
        String superToken = jwtAccessIssuer.issueSuperToken(savedUser.getId());

        return ResponseEntity.ok().body(superToken);
    }

    @Operation(summary = "익명 로그인", description = "익명 계정용 Access 토큰 발급")
    @ApiResponse(responseCode = "200", description = "익명 토큰 발급 성공")
    @GetMapping("/auth/anonymous")
    public ResponseEntity<String> anonymousLogin() {
        Users user = new Users();
        String nickname = nicknameGenerator.generateUniqueNickname();
        user.setUsername(nickname);
        user.setRole(JwtRoleType.ANONYMOUS);
        user.setProfileUrl("NULL");
        Users savedUser = usersRepository.save(user);
        String anonymousToken = jwtAccessIssuer.issueSuperToken(savedUser.getId());

        return ResponseEntity.ok().body(anonymousToken);
    }

}
