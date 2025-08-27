package com.example.demo.domain.playlist.service;

import com.example.demo.domain.playlist.dto.PlaylistGenre;
import com.example.demo.domain.playlist.dto.playlistdto.CursorPageResponse;
import com.example.demo.domain.playlist.dto.playlistdto.PageResponse;
import com.example.demo.domain.playlist.dto.search.PlaylistSearchResponse;
import com.example.demo.domain.playlist.dto.PlaylistSortOption;
import com.example.demo.domain.playlist.dto.search.CombinedSearchResponse;
import com.example.demo.domain.playlist.dto.search.PopularItem;
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

