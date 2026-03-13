package com.example.demo.domain.recommendation.dto.bundle;

import com.example.demo.domain.recommendation.entity.bundle.BundleTimeSlot;
import io.swagger.v3.oas.annotations.media.Schema;

public record CreateBundleRequest(

        @Schema(description = "시간대", example = "DAWN/MORNING/AFTERNOON/EVENING")
        BundleTimeSlot timeSlot,

        @Schema(description = "모음집 제목")
        String title

) {}
