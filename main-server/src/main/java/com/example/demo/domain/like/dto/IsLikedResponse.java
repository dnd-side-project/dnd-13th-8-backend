package com.example.demo.domain.like.dto;

import lombok.Builder;

@Builder
public record IsLikedResponse(boolean isLiked) {
}
