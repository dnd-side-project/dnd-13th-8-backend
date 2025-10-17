package com.example.demo.domain.cd.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record GetCdResponse(Long playlistId, List<CdItemResponse> cdItems) {
    public static GetCdResponse from(Long playlistId, List<CdItemResponse> cdItems) {
        return new GetCdResponse(playlistId, cdItems);
    }
}
