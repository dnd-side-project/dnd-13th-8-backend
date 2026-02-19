package com.example.demo.domain.playlist.service;

import com.example.demo.domain.playlist.dto.common.PlaylistGenre;
import com.example.demo.domain.playlist.dto.page.CursorPageResponse;
import com.example.demo.domain.playlist.dto.page.PageResponse;
import com.example.demo.domain.playlist.dto.search.*;
import com.example.demo.domain.playlist.dto.common.PlaylistSortOption;

import java.util.List;

public interface PlaylistSearchService {

    CursorPageResponse<PlaylistSearchResponse, Long> searchByGenre(
            PlaylistGenre genre,
            PlaylistSortOption sort,
            Long cursorId,
            Integer limit
    );

    List<PopularItem> getPopularTerms(String range, int limit);

    PageResponse<CombinedSearchResponse> searchByTitle(
            String query,
            PlaylistSortOption sort,
            int page,
            Integer size
    );

}

