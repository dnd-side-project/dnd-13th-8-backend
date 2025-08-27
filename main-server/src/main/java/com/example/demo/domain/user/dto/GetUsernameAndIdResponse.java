package com.example.demo.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "채팅용 유저 정보 응답 DTO")
@Builder
public record GetUsernameAndIdResponse (String userId, String username) {
}
