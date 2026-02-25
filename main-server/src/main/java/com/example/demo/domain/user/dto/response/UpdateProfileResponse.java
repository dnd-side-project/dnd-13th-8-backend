package com.example.demo.domain.user.dto.response;

import com.example.demo.domain.user.entity.Users;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "프로필 변경 응답 DTO")
public record UpdateProfileResponse(

        String userId,

        @Schema(description = "변경된 닉네임", example = "jun_dev")
        String nickname,

        @Schema(description = "변경된 프로필 아이디", example = "jun_dev")
        String shareCode,

        @Schema(description = "변경된 소개", example = "안녕하세요~")
        String bio,

        @Schema(description = "프로필 이미지")
        String profileImageUrl
) {
        public static UpdateProfileResponse from(Users user) {
                return new UpdateProfileResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getShareCode(),
                        user.getBio(),
                        user.getProfileUrl()
                );
        }
}
