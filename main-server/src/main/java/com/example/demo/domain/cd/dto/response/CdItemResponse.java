package com.example.demo.domain.cd.dto.response;

import com.example.demo.domain.cd.repository.projection.CdItemView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "CD 아이템 정보 (대표 CD 하나)")
public record CdItemResponse(

        @Schema(description = "CD 아이템 ID", example = "301")
        Long cdItemId,

        @Schema(description = "소품(prop) ID", example = "22")
        Long propId,

        @Schema(description = "X 좌표", example = "10")
        Long xCoordinate,

        @Schema(description = "Y 좌표", example = "20")
        Long yCoordinate,

        @Schema(description = "Z 좌표", example = "5")
        Long zCoordinate,

        @Schema(description = "회전 각도", example = "45")
        Long angle,

        @Schema(description = "CD 이미지 Presigned URL", example = "https://r2.bucket.com/cd-image.jpg?signature=abc123")
        String imageUrl
) {
    public static CdItemResponse of(CdItemView view, String imgUrl) {
        return CdItemResponse.builder()
                .cdItemId(view.cdId())
                .propId(view.propId())
                .xCoordinate(view.xCoordinate())
                .yCoordinate(view.yCoordinate())
                .zCoordinate(view.zCoordinate())
                .angle(view.angle())
                .imageUrl(imgUrl)
                .build();
    }
}
