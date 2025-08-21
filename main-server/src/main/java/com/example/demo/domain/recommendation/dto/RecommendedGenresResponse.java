package com.example.demo.domain.recommendation.dto;

import com.example.demo.domain.playlist.dto.GenreDto;
import java.util.List;

public record RecommendedGenresResponse(List<GenreDto> genres) {}