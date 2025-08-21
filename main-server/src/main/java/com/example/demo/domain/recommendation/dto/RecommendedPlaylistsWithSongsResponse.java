package com.example.demo.domain.recommendation.dto;

import java.util.List;

public record RecommendedPlaylistsWithSongsResponse (
    List<RecommendedPlaylistCard> recommendations)
{
}
