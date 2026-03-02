package com.example.demo.domain.cd.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record GetCdResponse(Long playlistId, List<CdItem> cdItems) {
    public static GetCdResponse from(Long playlistId, List<CdItem> cdItems) {
        return new GetCdResponse(playlistId, cdItems);
    }
}
