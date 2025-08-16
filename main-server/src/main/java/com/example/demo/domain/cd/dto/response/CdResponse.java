package com.example.demo.domain.cd.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record CdResponse(Long playlistId, List<CdItemResponse> cdItems) {
}
