package com.example.demo.domain.playlist.service;

import com.example.common.error.code.CommonErrorCode;
import com.example.common.error.exception.CdException;
import com.example.common.error.exception.PlaylistSearchException;
import com.example.demo.domain.cd.dto.response.CdResponse;
import com.example.demo.domain.cd.service.CdService;
import com.example.demo.domain.playlist.dto.PlaylistGenre;
import com.example.demo.domain.playlist.dto.PlaylistSortOption;
import com.example.demo.domain.playlist.dto.playlistdto.CursorPageResponse;
import com.example.demo.domain.playlist.dto.playlistdto.PageResponse;
import com.example.demo.domain.playlist.dto.search.*;
import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.playlist.repository.PlaylistRepository;
import com.example.demo.domain.user.repository.UsersRepository;
import com.example.demo.global.paging.CursorPageConverter;
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
            new PopularItem("여름"),
            new PopularItem("바캉스 플리"),
            new PopularItem("카페 재즈 플레이리스트"),
            new PopularItem("여름청량팝"),
            new PopularItem("감성 힙합"),
            new PopularItem("최맛 여자아이돌 모음"),
            new PopularItem("드라이브"),
            new PopularItem("K-POP"),
            new PopularItem("인디밴드음악")
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
        cursorId = (cursorId == null || cursorId < 1L) ? Long.MAX_VALUE : cursorId;

        try {
            SearchResult<Playlist> pages = playlistRepository
                    .findByGenreWithCursor(genre, sort, cursorId, finalLimit + 1);

            return CursorPageConverter.toCursorResponse(
                    pages.getResults(),
                    finalLimit,
                    p -> {
                        CdResponse cd = cdService.getOnlyCdByPlaylistId(p.getId());
                        return new PlaylistSearchResponse(
                                p.getId(),
                                p.getName(),
                                p.getUsers().getId(),
                                p.getUsers().getUsername(),
                                cd
                        );
                    },
                    PlaylistSearchResponse::playlistId, // 커서 추출
                    pages.getTotalCount()               // 총 개수
            );
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
            log.warn(" 검색어 기록 중 오류 발생: {}", e.getMessage());
        }

        try {
            SearchResult<PlaylistSearchDto> playlistsRaw = fetchPlaylistsWithCd(query, sort, offset, finalSize);
            // SearchResult<UserSearchDto> usersRaw = fetchUsers(query, sort, offset, finalSize); 유저 검색 비활성화

            List<SearchItem> merged = new ArrayList<>();
            merged.addAll(playlistsRaw.getResults());
            // merged.addAll(usersRaw.getResults());

            boolean hasNext = playlistsRaw.getResults().size() == finalSize; // || usersRaw.getResults().size() == finalSize;
            long totalCount = playlistsRaw.getTotalCount(); // + usersRaw.getTotalCount();

            return new PageResponse<>(
                    new CombinedSearchResponse(merged),
                    page,
                    finalSize,
                    hasNext,
                    totalCount
            );
        } catch (Exception e) {
            throw new PlaylistSearchException("제목 기반 검색 중 오류 발생", CommonErrorCode.BAD_REQUEST);
        }
    }

    private SearchResult<PlaylistSearchDto> fetchPlaylistsWithCd(String query, PlaylistSortOption sort, int offset, int limit) {
        SearchResult<PlaylistSearchDto> raw = playlistRepository
                .searchPlaylistsByTitleWithOffset(query, sort, offset, limit);

        List<PlaylistSearchDto> resolved = new ArrayList<>();
        for (PlaylistSearchDto item : raw.getResults()) {
            try {
                CdResponse cd = cdService.getOnlyCdByPlaylistId(item.playlistId());
                resolved.add(PlaylistSearchDto.from(
                        item.playlistId(),
                        item.playlistName(),
                        item.creatorId(),
                        item.creatorNickname(),
                        cd
                ));
            } catch (Exception e) {
                log.warn(" CD 정보 조회 실패: playlistId={} / {}", item.playlistId(), e.getMessage());
                throw new CdException("CD 정보 조회 실패", CommonErrorCode.BAD_REQUEST);
            }
        }
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
