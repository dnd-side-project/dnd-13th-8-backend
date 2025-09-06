package com.example.demo.global.kakao.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "카카오 로그인 요청 DTO")
public record KakaoLoginRequest(

        @Schema(description = "인가 코드 (카카오에서 전달받은 code)", example = "abc123def456")
        @NotBlank(message = "인가 코드는 필수입니다.")
        String code,

        @Schema(description = "PKCE 인증을 위한 code_verifier", example = "verifier123")
        @NotBlank(message = "code_verifier는 필수입니다.")
        String codeVerifier

) {}
