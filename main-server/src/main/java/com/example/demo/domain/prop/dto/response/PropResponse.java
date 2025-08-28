package com.example.demo.domain.prop.dto.response;

import lombok.Builder;

@Builder
public record PropResponse(Long propId, String theme, String imageUrl) {
}
