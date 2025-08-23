package com.example.demo.domain.playlist.controller;

import com.example.demo.domain.playlist.dto.PlaylistGenre;
import com.example.demo.domain.playlist.dto.search.PlaylistSearchResponse;
import com.example.demo.domain.playlist.dto.PlaylistSortOption;
import com.example.demo.domain.playlist.dto.search.CombinedSearchResponse;
import com.example.demo.domain.playlist.service.PlaylistSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/main/playlist/search")
@RequiredArgsConstructor
@Tag(name = "Playlist Search", description = "플레이리스트 검색 API")
public class PlaylistSearchController {

    private final PlaylistSearchService playlistSearchService;

    @Operation(
            summary = "장르 기반 플레이리스트 검색",
            description = "장르와 정렬 조건(RECENT/POPULAR)을 기반으로 플레이리스트를 검색합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "검색된 플레이리스트 목록",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = PlaylistSearchResponse.class)))
    )
    @GetMapping("/genre")
    public ResponseEntity<List<PlaylistSearchResponse>> searchByGenre(
            @Parameter(description = "정렬 조건", example = "POPULAR")
            @RequestParam(defaultValue = "RECENT") PlaylistSortOption sort,

            @Parameter(description = "플레이리스트 장르", example = "SLEEP")
            @RequestParam(name = "genre") PlaylistGenre genre,

            @Parameter(description = "결과 개수 제한 (optional)", example = "20")
            @RequestParam(name = "limit", required = false) Integer limit
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(playlistSearchService.searchByGenre(genre, sort, limit));
    }

    @Operation(
            summary = "제목 기반 플레이리스트 검색",
            description = "제목 키워드와 정렬 조건(RECENT/POPULAR)을 기반으로 플레이리스트를 검색합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "검색된 플레이리스트 목록",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = PlaylistSearchResponse.class)))
    )
    @GetMapping("/title")
    public ResponseEntity<CombinedSearchResponse> searchByTitle(
            @Parameter(description = "검색할 제목 키워드", example = "잔잔한")
            @RequestParam String query,

            @Parameter(description = "정렬 조건", example = "RECENT")
            @RequestParam(defaultValue = "RECENT") PlaylistSortOption sort,

            @Parameter(description = "결과 개수 제한 (optional)", example = "10")
            @RequestParam(required = false) Integer limit
    ) {
        CombinedSearchResponse combinedSearchResponse = playlistSearchService.searchAll(query, sort, limit);
        return ResponseEntity.status(HttpStatus.OK).body(combinedSearchResponse);
    }
}
