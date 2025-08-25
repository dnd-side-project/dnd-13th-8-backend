package com.example.demo.domain.cd.dto.response;

import com.example.demo.domain.cd.repository.projection.CdItemView;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
@Schema(description = "CD 아이템 정보 (대표 CD 하나)")
public record CdItemResponse(

        @Schema(description = "CD 아이템 ID", example = "301")
        Long cdItemId,

        @Schema(description = "장식(prop) ID", example = "22")
        Long propId,

        @Schema(description = "장식(prop) 테마", example = "배경")
        String theme,

        @Schema(description = "X 좌표", example = "10")
        Long xCoordinate,

        @Schema(description = "Y 좌표", example = "20")
        Long yCoordinate,

        @Schema(description = "Height", example = "5")
        Long height,

        @Schema(description = "width", example = "5")
        Long width,

        @Schema(description = "scale", example = "5")
        Long scale,

        @Schema(description = "회전 각도", example = "45")
        Long angle,

        @Schema(description = "CD 이미지 Presigned URL", example = "https://r2.bucket.com/cd-image.jpg?signature=abc123")
        String imageUrl
) {
    public static CdItemResponse of(CdItemView view, String imgUrl) {
        return CdItemResponse.builder()
                .cdItemId(view.cdId())
                .propId(view.propId())
                .theme(view.theme())
                .xCoordinate(view.xCoordinate())
                .yCoordinate(view.yCoordinate())
                .height(view.height())
                .width(view.width())
                .scale(view.scale())
                .angle(view.angle())
                .imageUrl(imgUrl)
                .build();
    }

    public static List<CdItemResponse> fromJsonList(String cdItemsJson) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(cdItemsJson, new TypeReference<>() {});
        } catch (Exception e) {
            throw new RuntimeException("CdItemResponse 리스트로 변환 중 오류", e);
        }
    }
}
