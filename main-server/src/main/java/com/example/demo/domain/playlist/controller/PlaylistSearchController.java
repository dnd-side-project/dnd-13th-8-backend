package com.example.demo.domain.playlist.controller;

import com.example.demo.domain.playlist.dto.PlaylistGenre;
import com.example.demo.domain.playlist.dto.playlistdto.CursorPageResponse;
import com.example.demo.domain.playlist.dto.search.PlaylistSearchResponse;
import com.example.demo.domain.playlist.dto.PlaylistSortOption;
import com.example.demo.domain.playlist.dto.search.CombinedSearchResponse;
import com.example.demo.domain.playlist.dto.search.PopularItem;
import com.example.demo.domain.playlist.dto.search.PopularSearchResponse;
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
            summary = "장르 기반 플레이리스트 검색 (커서 기반)",
            description = "장르와 정렬 조건(RECENT/POPULAR)을 기반으로 플레이리스트를 검색합니다. "
                    + "커서 기반 페이지네이션(cursorId, limit)을 지원합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "검색된 플레이리스트 목록 (커서 기반 응답)",
            content = @Content(schema = @Schema(implementation = CursorPageResponse.class))
    )
    @GetMapping("/genre")
    public ResponseEntity<CursorPageResponse<PlaylistSearchResponse>> searchByGenre(
            @Parameter(description = "정렬 조건", example = "POPULAR")
            @RequestParam(defaultValue = "RECENT") PlaylistSortOption sort,

            @Parameter(description = "플레이리스트 장르", example = "SLEEP")
            @RequestParam(name = "genre") PlaylistGenre genre,

            @Parameter(description = "마지막으로 조회한 playlistId(최초 요청은 생략)", example = "123")
            @RequestParam(name = "cursorId", required = false) Long cursorId,

            @Parameter(description = "한 페이지에 가져올 개수", example = "10")
            @RequestParam(name = "limit", defaultValue = "10") Integer limit
    ) {
        CursorPageResponse<PlaylistSearchResponse> response =
                playlistSearchService.searchByGenre(genre, sort, cursorId, limit);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "제목 기반 플레이리스트 검색 (커서 기반)",
            description = "제목 키워드와 정렬 조건(RECENT/POPULAR)을 기반으로 플레이리스트를 검색합니다. "
                    + "커서 기반 페이지네이션(cursorId, limit)을 지원합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "검색된 플레이리스트 목록 (커서 기반 응답)",
            content = @Content(schema = @Schema(implementation = CursorPageResponse.class))
    )
    @GetMapping("/title")
    public ResponseEntity<CursorPageResponse<PlaylistSearchResponse>> searchByTitle(
            @Parameter(description = "검색할 제목 키워드", example = "잔잔한")
            @RequestParam String query,

            @Parameter(description = "정렬 조건", example = "RECENT")
            @RequestParam(defaultValue = "RECENT") PlaylistSortOption sort,

            @Parameter(description = "마지막으로 조회한 playlistId(최초 요청은 생략)", example = "123")
            @RequestParam(name = "cursorId", required = false) Long cursorId,

            @Parameter(description = "한 페이지에 가져올 개수", example = "10")
            @RequestParam(name = "limit", defaultValue = "10") Integer limit
    ) {
        CursorPageResponse<PlaylistSearchResponse> response =
                playlistSearchService.searchByTitle(query, sort, cursorId, limit);
        return ResponseEntity.ok(response);
    }


    @Operation(
            summary = "인기 검색어 조회 (커서 기반)",
            description = """
                Redis에 저장된 검색어를 조회 수 기준으로 정렬하여 반환합니다.
                range는 'today', '7d', '30d' 중 하나로 지정할 수 있으며, 기본값은 'today'입니다.
                Redis 데이터가 없을 경우 기본 디폴트 검색어가 fallback으로 제공됩니다.
           
            """
    )
    @ApiResponse(
            responseCode = "200",
            description = "인기 검색어 조회 성공",
            content = @Content(schema = @Schema(implementation = PopularSearchResponse.class))
    )
    @GetMapping("/popular")
    public ResponseEntity<PopularSearchResponse> getPopularSearchTerms(
            @Parameter(description = "조회 범위 (today, 7d, 30d)", example = "today")
            @RequestParam(defaultValue = "today") String range,


            @Parameter(description = "최대 검색어 개수", example = "10")
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<PopularItem> terms = playlistSearchService.getPopularTerms(range, limit);
        PopularSearchResponse response = new PopularSearchResponse(range, limit, terms);
        return ResponseEntity.ok(response);
    }
}
