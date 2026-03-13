package com.example.demo.domain.recommendation.dto.bundle;

import com.example.demo.domain.recommendation.entity.bundle.BundleTimeSlot;
import com.example.demo.domain.recommendation.entity.bundle.Bundle;
import io.swagger.v3.oas.annotations.media.Schema;

public record CreateBundleResponse(

        @Schema(description = "모음집 ID")
        Long id,

        @Schema(description = "시간대")
        BundleTimeSlot timeSlot,

        @Schema(description = "모음집 제목")
        String title

) {

    public static CreateBundleResponse from(Bundle bundle) {
        return new CreateBundleResponse(
                bundle.getId(),
                bundle.getTimeSlot(),
                bundle.getTitle()
        );
    }
}