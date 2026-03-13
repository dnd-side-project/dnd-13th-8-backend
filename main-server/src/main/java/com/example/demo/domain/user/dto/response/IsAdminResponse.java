package com.example.demo.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record IsAdminResponse(
        @Schema(description = "관리자 여부")
        boolean isAdmin
) {}