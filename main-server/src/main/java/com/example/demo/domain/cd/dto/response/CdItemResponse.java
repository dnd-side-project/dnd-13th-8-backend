package com.example.demo.domain.cd.dto.response;

import lombok.Builder;

@Builder
public record CdItemResponse(Long cdItemId, Long propId,
                             Long xCoordinate, Long yCoordinate, Long zCoordinate, Long angle,
                             String imageUrl) {
}
