package com.example.demo.domain.cd.dto.response;

import lombok.Builder;
import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "플레이리스트의 CD 응답")
@Builder
public record CdResponse(
        @Schema(description = "CD에 포함된 아이템 리스트")
        List<CdItemResponse> cdItems
) {
}
