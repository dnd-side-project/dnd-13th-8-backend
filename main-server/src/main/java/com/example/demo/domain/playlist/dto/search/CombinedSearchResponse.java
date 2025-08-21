package com.example.demo.domain.playlist.dto.search;

import java.util.List;

public record CombinedSearchResponse(
        List<SearchItem> results
) {
}
