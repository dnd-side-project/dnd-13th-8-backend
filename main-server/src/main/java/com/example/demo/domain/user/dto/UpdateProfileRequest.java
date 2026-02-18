package com.example.demo.domain.user.dto;

import com.example.demo.domain.user.entity.MusicKeyword;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Schema(description = "프로필 변경 요청 DTO")
public record UpdateProfileRequest(
        @Schema(description = "새 닉네임", example = "지구젤리")
        String nickname,

        @Schema(description = "새 프로필 이미지 파일")
        MultipartFile profileImage,

        @Schema(description = "음악 취향 키워드")
        List<MusicKeyword> musicKeywords
) {}
