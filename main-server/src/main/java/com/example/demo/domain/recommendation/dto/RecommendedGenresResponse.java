package com.example.demo.domain.recommendation.dto;

import com.example.demo.domain.playlist.dto.GenreDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "추천 장르 응답 DTO")
public record RecommendedGenresResponse(

        @Schema(description = "추천 장르 목록")
        List<GenreDto> genres

) {}
