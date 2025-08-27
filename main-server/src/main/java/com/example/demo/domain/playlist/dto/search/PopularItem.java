package com.example.demo.domain.playlist.dto.search;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "인기 검색어 항목")
public record PopularItem(
        @Schema(description = "검색어", example = "여름")
        String term
) {}
