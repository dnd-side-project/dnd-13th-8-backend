package com.example.demo.domain.playlist.service;

import com.example.demo.domain.playlist.dto.PlaylistGenre;
import com.example.demo.domain.playlist.dto.playlistdto.CursorPageResponse;
import com.example.demo.domain.playlist.dto.playlistdto.PageResponse;
import com.example.demo.domain.playlist.dto.search.*;
import com.example.demo.domain.playlist.dto.PlaylistSortOption;

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

