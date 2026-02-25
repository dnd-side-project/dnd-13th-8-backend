package com.example.demo.global.kakao.dto;

import com.example.demo.domain.user.entity.Users;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "카카오 로그인 응답")
public record KakaoLoginResponse(

        @Schema(description = "사용자 ID", example = "0ca97df3-aff8-4e3f-b849-01a53a87dab4")
        String userId,

        @Schema(description = "사용자 이름", example = "홍길동")
        String username,

        @Schema(description = "사용자 아이디", example = "hong_gil")
        String shareCode,

        @Schema(description = "사용자 프로필 이미지 URL", example = "https://cdn.example.com/profile/user-uuid-1234.jpg")
        String userProfileImageUrl,

        @Schema(description = "JWT Access Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String jwtAccessToken
) {
        public static KakaoLoginResponse from(Users user, String jwtAccessToken) {
                return new KakaoLoginResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getShareCode(),
                        user.getProfileUrl(),
                        jwtAccessToken
                );
        }
}
