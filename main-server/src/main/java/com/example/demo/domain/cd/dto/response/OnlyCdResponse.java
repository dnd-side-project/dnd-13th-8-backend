package com.example.demo.domain.cd.dto.response;

import lombok.Builder;
import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "CD에 포함된 아이템 목록 응답 DTO")
@Builder
public record OnlyCdResponse(
        @Schema(description = "CD에 포함된 아이템 리스트")
        List<CdItemResponse> cdItems
) {
}
