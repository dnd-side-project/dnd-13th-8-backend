package com.example.demo.domain.playlist.service.search;

import com.example.common.error.code.CommonErrorCode;
import com.example.common.error.exception.PlaylistSearchException;
import com.example.demo.domain.cd.dto.response.CdItemsByPlaylist;
import com.example.demo.domain.cd.service.CdService;
import com.example.demo.domain.playlist.dto.common.PlaylistGenre;
import com.example.demo.domain.playlist.dto.common.PlaylistSortOption;
import com.example.demo.domain.playlist.dto.feed.PlaylistCursor;
import com.example.demo.global.paging.CursorPageResponse;
import com.example.demo.global.paging.PageResponse;
import com.example.demo.domain.playlist.dto.search.*;
import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.playlist.repository.PlaylistRepository;
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

    private final StringRedisTemplate redis;
    private final UsersRepository usersRepository;
    private final PlaylistRepository playlistRepository;
    private final CdService cdService;

    private static final List<PopularItem> DEFAULT_POPULAR_TERMS = List.of(
            new PopularItem("새해"),
            new PopularItem("가족"),
            new PopularItem("2026"),
            new PopularItem("돈"),
            new PopularItem("겨울"),
            new PopularItem("연초"),
            new PopularItem("새학기"),
            new PopularItem("봄"),
            new PopularItem("ASMR"),
            new PopularItem("여행"),
            new PopularItem("노동요"),
            new PopularItem("시험"),
            new PopularItem("OST"),
            new PopularItem("휴식")
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

        try {
            PlaylistCursor decodedCursor = null;

            if (cursorId != null) {
                Playlist pivot = playlistRepository.findById(cursorId)
                        .orElseThrow(() -> new PlaylistSearchException(
                                "커서 플레이리스트를 찾을 수 없습니다.",
                                CommonErrorCode.BAD_REQUEST
                        ));

                decodedCursor = switch (sort) {
                    case RECENT -> new PlaylistCursor(pivot.getId(), null);
                    case POPULAR -> new PlaylistCursor(pivot.getId(), pivot.getVisitCount());
                };
            }

            SearchResult<Playlist> pages = playlistRepository
                    .findByGenreWithCursor(genre, sort, decodedCursor, finalLimit);

            List<Playlist> fetched = pages.getResults();

            boolean hasNext = fetched.size() > finalLimit;
            List<Playlist> page = hasNext ? fetched.subList(0, finalLimit) : fetched;

            List<Long> playlistIds = page.stream()
                    .map(Playlist::getId)
                    .toList();

            CdItemsByPlaylist cdItemsByPlaylist = cdService.findCdItemsByPlaylistIdsIn(playlistIds);

            List<PlaylistSearchResponse> content = page.stream()
                    .map(p -> new PlaylistSearchResponse(
                            p.getId(),
                            p.getName(),
                            p.getUsers().getId(),
                            p.getUsers().getUsername(),
                            cdItemsByPlaylist.cdItemsOf(p.getId())
                    ))
                    .toList();

            Long nextCursor = null;
            if (hasNext && !page.isEmpty()) {
                nextCursor = page.get(page.size() - 1).getId();
            }

            return new CursorPageResponse<>(
                    content,
                    nextCursor,
                    content.size(),
                    hasNext,
                    pages.getTotalCount()
            );
        } catch (PlaylistSearchException e) {
            throw e;
        } catch (Exception e) {
            throw new PlaylistSearchException("장르 기반 검색 중 오류 발생", CommonErrorCode.BAD_REQUEST);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CombinedSearchResponse> searchByTitle(
            String query, PlaylistSortOption sort, int page, Integer size
    ) {
        int finalSize = validateLimit(size);
        int offset = page * finalSize;

        try {
            recordSearchTerm(query);
        } catch (Exception e) {
            log.warn("검색어 기록 중 오류 발생: {}", e.getMessage());
        }

        try {
            SearchResult<UserSearchDto> usersPage = fetchUsers(query, sort, offset, finalSize);
            long usersTotal = usersPage.getTotalCount();

            List<SearchItem> merged = new ArrayList<>(finalSize);
            merged.addAll(usersPage.getResults());

            int remaining = finalSize - usersPage.getResults().size();

            long playlistsTotal;
            if (remaining > 0) {
                int playlistOffset = (int) Math.max(0L, offset - usersTotal);
                SearchResult<PlaylistSearchDto> playlistsPage =
                        fetchPlaylistsWithCd(query, sort, playlistOffset, remaining);

                merged.addAll(playlistsPage.getResults());
                playlistsTotal = playlistsPage.getTotalCount();
            } else {
                playlistsTotal = playlistRepository.countPlaylistByTitle(query);
            }

            long totalCount = usersTotal + playlistsTotal;
            boolean hasNext = (offset + finalSize) < totalCount;

            return new PageResponse<>(
                    new CombinedSearchResponse(merged),
                    page,
                    finalSize,
                    hasNext,
                    totalCount
            );
        } catch (Exception e) {
            log.error("제목 기반 검색 중 오류 발생", e);
            throw new PlaylistSearchException("제목 기반 검색 중 오류 발생", CommonErrorCode.BAD_REQUEST);
        }
    }

    private SearchResult<PlaylistSearchDto> fetchPlaylistsWithCd(String query, PlaylistSortOption sort, int offset, int limit) {
        SearchResult<PlaylistSearchDto> raw =
                playlistRepository.searchPlaylistsByTitleWithOffset(query, sort, offset, limit);

        if (raw.getResults().isEmpty()) {
            return raw;
        }

        List<Long> playlistIds = raw.getResults().stream()
                .map(PlaylistSearchDto::playlistId)
                .toList();

        CdItemsByPlaylist cdItemsByPlaylist = cdService.findCdItemsByPlaylistIdsIn(playlistIds);

        List<PlaylistSearchDto> resolved = raw.getResults().stream()
                .map(item -> item.withCdResponse(
                        cdItemsByPlaylist.cdItemsOf(item.playlistId())
                ))
                .toList();

        return new SearchResult<>(resolved, raw.getTotalCount());
    }

    private SearchResult<UserSearchDto> fetchUsers(String query, PlaylistSortOption sort, int offset, int limit) {
        SearchResult<UserSearchDto> result = usersRepository.searchUsersByQueryWithOffset(query, sort, offset, limit);
        return new SearchResult<>(result.getResults(), result.getTotalCount());
    }


    @Override
    public List<PopularItem> getPopularTerms(String range, int limit) {
        String redisKey = resolveKeyFromRange(range);
        Set<TypedTuple<String>> raw = redis.opsForZSet().reverseRangeWithScores(redisKey, 0, limit - 1);

        if (raw == null || raw.isEmpty()) {
            return DEFAULT_POPULAR_TERMS.stream().limit(limit).toList();
        }

        return raw.stream()
                .map(tuple -> new PopularItem(tuple.getValue()))
                .toList();
    }

    private int validateLimit(Integer limit) {
        if (limit != null && limit > 0 && limit <= 50) {
            return limit;
        }
        return 10;
    }

    private String resolveKeyFromRange(String range) {
        LocalDate now = LocalDate.now(ZoneId.of("Asia/Seoul"));
        return switch (range) {
            case "7d" -> "pop:term:" + now.minusDays(7) + ":" + now;
            case "30d" -> "pop:term:" + now.minusDays(30) + ":" + now;
            default -> "pop:term:" + now;
        };
    }

    public void recordSearchTerm(String rawTerm) {
        String term = normalize(rawTerm);
        String todayKey = "pop:term:" + LocalDate.now(ZoneId.of("Asia/Seoul"));

        redis.opsForZSet().incrementScore(todayKey, term, 1.0);
        redis.expire(todayKey, Duration.ofDays(35));
    }

    public String normalize(String term) {
        return Normalizer.normalize(
                term.trim().replaceAll("\\s+", " "),
                Normalizer.Form.NFKC
        ).toLowerCase(Locale.ROOT);
    }
}
