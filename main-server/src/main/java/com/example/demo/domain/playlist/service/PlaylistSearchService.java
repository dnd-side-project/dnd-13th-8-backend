package com.example.demo.domain.playlist.service;

import com.example.demo.domain.playlist.dto.PlaylistGenre;
import com.example.demo.domain.playlist.dto.search.PlaylistSearchResponse;
import com.example.demo.domain.playlist.dto.PlaylistSortOption;
import com.example.demo.domain.playlist.dto.search.CombinedSearchResponse;
import com.example.demo.domain.playlist.dto.search.PopularItem;
import java.util.List;

public interface PlaylistSearchService {

    List<PlaylistSearchResponse> searchByGenre(PlaylistGenre genre, PlaylistSortOption sort, Integer limit);

    CombinedSearchResponse searchAll(String query, PlaylistSortOption sort,int limit);

    List<PopularItem> getPopularTerms(String range, int limit);

}

