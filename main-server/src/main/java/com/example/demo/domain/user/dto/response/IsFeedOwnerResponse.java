package com.example.demo.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "피드 주인인지 여부 확인")
public record IsFeedOwnerResponse(boolean isOwner) {
}
