package com.example.demo.domain.playlist.service.search;

import com.example.common.error.code.CommonErrorCode;
import com.example.common.error.exception.PlaylistSearchException;
import com.example.demo.domain.cd.dto.response.CdItemsByPlaylist;
import com.example.demo.domain.cd.service.CdService;
import com.example.demo.domain.playlist.dto.common.PlaylistGenre;
import com.example.demo.domain.playlist.dto.common.PlaylistSortOption;
import com.example.demo.domain.playlist.dto.feed.PlaylistCursor;
import com.example.demo.domain.playlist.repository.query.PlaylistSearchQueryRepository;
import com.example.demo.global.paging.CursorPageResponse;
import com.example.demo.global.paging.PageResponse;
import com.example.demo.domain.playlist.dto.search.*;
import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.playlist.repository.command.PlaylistRepository;
import com.example.demo.domain.user.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaylistSearchServiceImpl implements PlaylistSearchService {

    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 50;
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private final StringRedisTemplate redis;
    private final UsersRepository usersRepository;
    private final PlaylistSearchQueryRepository playlistSearchQueryRepository;
    private final PlaylistRepository playlistRepository;
    private final CdService cdService;

    private static final List<PopularItem> DEFAULT_POPULAR_TERMS = List.of(
            new PopularItem("JPOP"),
            new PopularItem("출근"),
            new PopularItem("2026"),
            new PopularItem("힐링"),
            new PopularItem("집중"),
            new PopularItem("애니"),
            new PopularItem("플레이리스트"),
            new PopularItem("봄"),
            new PopularItem("ASMR"),
            new PopularItem("벚꽃"),
            new PopularItem("드라이브"),
            new PopularItem("케이팝"),
            new PopularItem("OST"),
            new PopularItem("팝"),
            new PopularItem("여름")
    );

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponse<PlaylistSearchResponse, Long> searchByGenre(
            PlaylistGenre genre,
            PlaylistSortOption sort,
            Long cursorId,
            Integer limit
    ) {
        int finalLimit = validateLimit(limit);

        return executeSearch(
                () -> {
                    PlaylistCursor cursor = decodeCursor(cursorId, sort);
                    SearchResult<Playlist> result =
                            playlistSearchQueryRepository.findByGenreWithCursor(
                                    genre,
                                    sort,
                                    cursor,
                                    finalLimit
                            );

                    SliceResult<Playlist> slice = slice(result.getResults(), finalLimit);
                    CdItemsByPlaylist cdItemsByPlaylist = loadCdItems(slice.page());

                    List<PlaylistSearchResponse> content = slice.page().stream()
                            .map(playlist -> toPlaylistSearchResponse(playlist, cdItemsByPlaylist))
                            .toList();

                    return new CursorPageResponse<>(
                            content,
                            resolveNextCursor(slice.page(), slice.hasNext()),
                            content.size(),
                            slice.hasNext(),
                            result.getTotalCount()
                    );
                },
                "장르 기반 검색 중 오류 발생"
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CombinedSearchResponse> searchByTitle(
            String query,
            PlaylistSortOption sort,
            int page,
            Integer size
    ) {
        int finalSize = validateLimit(size);
        int offset = page * finalSize;

        recordSearchTermSafely(query);

        return executeSearch(
                () -> {
                    SearchResult<UserSearchDto> usersPage = fetchUsers(query, sort, offset, finalSize);
                    long usersTotal = usersPage.getTotalCount();

                    int remainingSize = finalSize - usersPage.getResults().size();
                    PlaylistSearchPage playlistPage = fetchPlaylistPage(
                            query,
                            sort,
                            offset,
                            remainingSize,
                            usersTotal
                    );

                    List<SearchItem> merged = mergeSearchItems(
                            usersPage.getResults(),
                            playlistPage.items()
                    );

                    long totalCount = usersTotal + playlistPage.totalCount();
                    boolean hasNext = (offset + finalSize) < totalCount;

                    return new PageResponse<>(
                            new CombinedSearchResponse(merged),
                            page,
                            finalSize,
                            hasNext,
                            totalCount
                    );
                },
                "제목 기반 검색 중 오류 발생"
        );
    }

    @Override
    public List<PopularItem> getPopularTerms(String range, int limit) {
        String redisKey = resolveKeyFromRange(range);
        Set<TypedTuple<String>> raw = redis.opsForZSet()
                .reverseRangeWithScores(redisKey, 0, limit - 1);

        return Optional.ofNullable(raw)
                .filter(values -> !values.isEmpty())
                .map(values -> values.stream()
                        .map(tuple -> new PopularItem(tuple.getValue()))
                        .toList()
                )
                .orElseGet(() -> DEFAULT_POPULAR_TERMS.stream().limit(limit).toList());
    }

    public void recordSearchTerm(String rawTerm) {
        String term = normalize(rawTerm);
        String todayKey = "pop:term:" + LocalDate.now(SEOUL);

        redis.opsForZSet().incrementScore(todayKey, term, 1.0);
        redis.expire(todayKey, Duration.ofDays(35));
    }

    public String normalize(String term) {
        return Normalizer.normalize(
                term.trim().replaceAll("\\s+", " "),
                Normalizer.Form.NFKC
        ).toLowerCase(Locale.ROOT);
    }

    private <T> T executeSearch(SearchExecutor<T> executor, String errorMessage) {
        try {
            return executor.execute();
        } catch (PlaylistSearchException e) {
            throw e;
        } catch (Exception e) {
            log.error(errorMessage, e);
            throw new PlaylistSearchException(errorMessage, CommonErrorCode.BAD_REQUEST);
        }
    }

    private PlaylistCursor decodeCursor(Long cursorId, PlaylistSortOption sort) {
        if (cursorId == null) {
            return null;
        }

        Playlist pivot = playlistRepository.findById(cursorId)
                .orElseThrow(() -> new PlaylistSearchException(
                        "커서 플레이리스트를 찾을 수 없습니다.",
                        CommonErrorCode.BAD_REQUEST
                ));

        return switch (sort) {
            case RECENT -> new PlaylistCursor(pivot.getId(), null);
            case POPULAR -> new PlaylistCursor(pivot.getId(), pivot.getVisitCount());
        };
    }

    private SliceResult<Playlist> slice(List<Playlist> fetched, int limit) {
        boolean hasNext = fetched.size() > limit;
        List<Playlist> page = hasNext ? fetched.subList(0, limit) : fetched;
        return new SliceResult<>(page, hasNext);
    }

    private Long resolveNextCursor(List<Playlist> page, boolean hasNext) {
        return hasNext && !page.isEmpty()
                ? page.get(page.size() - 1).getId()
                : null;
    }

    private CdItemsByPlaylist loadCdItems(List<Playlist> playlists) {
        List<Long> playlistIds = playlists.stream()
                .map(Playlist::getId)
                .toList();

        if (playlistIds.isEmpty()) {
            return CdItemsByPlaylist.empty();
        }

        return cdService.findCdItemsByPlaylistIdsIn(playlistIds);
    }

    private PlaylistSearchResponse toPlaylistSearchResponse(
            Playlist playlist,
            CdItemsByPlaylist cdItemsByPlaylist
    ) {
        return new PlaylistSearchResponse(
                playlist.getId(),
                playlist.getName(),
                playlist.getUsers().getId(),
                playlist.getUsers().getUsername(),
                cdItemsByPlaylist.cdItemsOf(playlist.getId())
        );
    }

    private PlaylistSearchPage fetchPlaylistPage(
            String query,
            PlaylistSortOption sort,
            int offset,
            int remainingSize,
            long usersTotal
    ) {
        if (remainingSize <= 0) {
            return new PlaylistSearchPage(
                    List.of(),
                    playlistSearchQueryRepository.countPlaylistByTitle(query)
            );
        }

        int playlistOffset = (int) Math.max(0L, offset - usersTotal);
        SearchResult<PlaylistSearchDto> result =
                fetchPlaylistsWithCd(query, sort, playlistOffset, remainingSize);

        return new PlaylistSearchPage(result.getResults(), result.getTotalCount());
    }

    private SearchResult<PlaylistSearchDto> fetchPlaylistsWithCd(
            String query,
            PlaylistSortOption sort,
            int offset,
            int limit
    ) {
        SearchResult<PlaylistSearchDto> raw =
                playlistSearchQueryRepository.searchPlaylistsByTitleWithOffset(
                        query,
                        sort,
                        offset,
                        limit
                );

        if (raw.getResults().isEmpty()) {
            return raw;
        }

        List<Long> playlistIds = raw.getResults().stream()
                .map(PlaylistSearchDto::playlistId)
                .toList();

        CdItemsByPlaylist cdItemsByPlaylist =
                cdService.findCdItemsByPlaylistIdsIn(playlistIds);

        List<PlaylistSearchDto> resolved = raw.getResults().stream()
                .map(item -> item.withCdResponse(
                        cdItemsByPlaylist.cdItemsOf(item.playlistId())
                ))
                .toList();

        return new SearchResult<>(resolved, raw.getTotalCount());
    }

    private SearchResult<UserSearchDto> fetchUsers(
            String query,
            PlaylistSortOption sort,
            int offset,
            int limit
    ) {
        return usersRepository.searchUsersByQueryWithOffset(query, sort, offset, limit);
    }

    private List<SearchItem> mergeSearchItems(
            List<UserSearchDto> users,
            List<PlaylistSearchDto> playlists
    ) {
        List<SearchItem> merged = new ArrayList<>(users.size() + playlists.size());
        merged.addAll(users);
        merged.addAll(playlists);
        return merged;
    }

    private void recordSearchTermSafely(String query) {
        try {
            recordSearchTerm(query);
        } catch (Exception e) {
            log.warn("검색어 기록 중 오류 발생: {}", e.getMessage());
        }
    }

    private int validateLimit(Integer limit) {
        if (limit != null && limit > 0 && limit <= MAX_LIMIT) {
            return limit;
        }
        return DEFAULT_LIMIT;
    }

    private String resolveKeyFromRange(String range) {
        LocalDate now = LocalDate.now(SEOUL);

        return switch (range) {
            case "7d" -> "pop:term:" + now.minusDays(7) + ":" + now;
            case "30d" -> "pop:term:" + now.minusDays(30) + ":" + now;
            default -> "pop:term:" + now;
        };
    }

    @FunctionalInterface
    private interface SearchExecutor<T> {
        T execute();
    }

    private record SliceResult<T>(List<T> page, boolean hasNext) {
    }

    private record PlaylistSearchPage(List<PlaylistSearchDto> items, long totalCount) {
    }
}
