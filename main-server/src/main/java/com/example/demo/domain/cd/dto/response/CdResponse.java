package com.example.demo.domain.cd.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record CdResponse(Long playlistId, List<CdItemResponse> cdItems) {
    public static CdResponse from(Long playlistId, List<CdItemResponse> cdItems) {
        return new CdResponse(playlistId, cdItems);
    }
}
