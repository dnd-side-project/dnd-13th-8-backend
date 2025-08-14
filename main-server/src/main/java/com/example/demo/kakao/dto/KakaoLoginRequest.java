package com.example.demo.kakao.dto;

import jakarta.validation.constraints.NotBlank;

public record KakaoLoginRequest(
        @NotBlank(message = "인가 코드는 필수입니다.")
        String code,
        @NotBlank(message = "code_verifier는 필수입니다.")
        String codeVerifier
) {}
