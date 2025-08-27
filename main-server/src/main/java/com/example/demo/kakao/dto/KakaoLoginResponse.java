package com.example.demo.kakao.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "카카오 로그인 응답")
public record KakaoLoginResponse(

        @Schema(description = "사용자 ID", example = "0ca97df3-aff8-4e3f-b849-01a53a87dab4")
        String userId,

        @Schema(description = "사용자 이름", example = "홍길동")
        String username,

        @Schema(description = "사용자 프로필 이미지 URL", example = "https://cdn.example.com/profile/user-uuid-1234.jpg")
        String userProfileImageUrl,

        @Schema(description = "JWT Access Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String jwtAccessToken
) {}
