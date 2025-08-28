package com.example.demo.domain.playlist.dto.search;

import java.util.List;

public class SearchResult<T> {

    private final List<T> results;
    private final long totalCount;

    public SearchResult(List<T> results, long totalCount) {
        this.results = results;
        this.totalCount = totalCount;
    }

    public List<T> getResults() {
        return results;
    }

    public long getTotalCount() {
        return totalCount;
    }
}
