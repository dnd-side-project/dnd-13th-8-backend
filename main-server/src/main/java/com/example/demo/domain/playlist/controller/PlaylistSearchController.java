package com.example.demo.domain.playlist.controller;

import com.example.demo.domain.playlist.dto.PlaylistGenre;
import com.example.demo.domain.playlist.dto.PlaylistSearchResponse;
import com.example.demo.domain.playlist.dto.PlaylistSortOption;
import com.example.demo.domain.playlist.service.PlaylistSearchService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/main/playlist/search")
@RequiredArgsConstructor
public class PlaylistSearchController {

    private final PlaylistSearchService playlistSearchService;

    @GetMapping("/genre")
    public List<PlaylistSearchResponse> searchByGenre(
            @RequestParam(defaultValue = "RECENT") PlaylistSortOption sort,
            @RequestParam(name = "genre") PlaylistGenre genre,
            @RequestParam(name = "limit", required = false) Integer limit
    ) {
        return playlistSearchService.searchByGenre(genre,sort, limit);
    }

    @GetMapping("/title")
    public List<PlaylistSearchResponse> searchByTitle(
            @RequestParam String query,
            @RequestParam(defaultValue = "RECENT") PlaylistSortOption sort,
            @RequestParam(required = false) Integer limit
    ) {
        return playlistSearchService.searchByTitle(query, sort, limit);
    }
}
