package com.example.demo.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;

@Schema(description = "프로필 변경 요청 DTO")
public record UpdateProfileRequest(
        @Schema(description = "새 닉네임", example = "jun_dev")
        String nickname,

        @Schema(description = "새 프로필 이미지 파일")
        MultipartFile profileImage
) {}
