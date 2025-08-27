package com.example.demo.domain.playlist.service;

import com.example.demo.domain.playlist.dto.PlaylistGenre;
import com.example.demo.domain.playlist.dto.PlaylistSortOption;
import com.example.demo.domain.playlist.dto.playlistdto.CursorPageResponse;
import com.example.demo.domain.playlist.dto.playlistdto.PageResponse;
import com.example.demo.domain.playlist.dto.search.CombinedSearchResponse;
import com.example.demo.domain.playlist.dto.search.PlaylistSearchDto;
import com.example.demo.domain.playlist.dto.search.PlaylistSearchResponse;
import com.example.demo.domain.playlist.dto.search.PopularItem;
import com.example.demo.domain.playlist.dto.search.SearchItem;
import com.example.demo.domain.playlist.dto.search.UserSearchDto;
import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.representative.entity.RepresentativePlaylist;
import com.example.demo.domain.representative.repository.RepresentativeRepresentativePlaylistRepository;
import com.example.demo.domain.user.repository.UsersRepository;
import java.text.Normalizer;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaylistSearchServiceImpl implements PlaylistSearchService {

    private final RepresentativeRepresentativePlaylistRepository representativePlaylistRepository;
    private final StringRedisTemplate redis;
    private final UsersRepository usersRepository;

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
    public CursorPageResponse<PlaylistSearchResponse> searchByGenre(
            PlaylistGenre genre,
            PlaylistSortOption sort,
            Long cursorId,
            Integer limit
    ) {
        int finalLimit = validateLimit(limit);
        cursorId = (cursorId == null || cursorId < 1L) ? Long.MAX_VALUE : cursorId;

        List<RepresentativePlaylist> reps = representativePlaylistRepository.findByGenreWithCursor(genre, sort,cursorId, limit);

        CursorPageResponse<PlaylistSearchResponse> response = toCursorResponse(
                reps,
                finalLimit,
                rep -> {
                    Playlist p = rep.getPlaylist();
                    return new PlaylistSearchResponse(
                            p.getId(),
                            p.getName(),
                            p.getUsers().getId(),
                            p.getUsers().getUsername()
                    );
                },
                PlaylistSearchResponse::playlistId
        );
        return response;
    }


    @Override
    @Transactional(readOnly = true)
    public PageResponse<CombinedSearchResponse> searchByTitle(String query, PlaylistSortOption sort, int page, Integer size
    ) {
        int finalSize = validateLimit(size);
        int offset = page * finalSize;

        recordSearchTerm(query);

        List<PlaylistSearchDto> playlists =
                representativePlaylistRepository.searchPlaylistsByTitleWithOffset(query, sort, offset, finalSize);
        List<UserSearchDto> users =
                usersRepository.searchUsersByQueryWithOffset(query, sort, offset, finalSize);

        List<SearchItem> merged = new ArrayList<>();
        merged.addAll(playlists);
        merged.addAll(users);

        boolean hasNext = playlists.size() == finalSize || users.size() == finalSize;

        return new PageResponse<>(
                new CombinedSearchResponse(merged),
                page,
                finalSize,
                hasNext
        );
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

    /*
    내부 메소드
     */

    private int validateLimit(Integer limit) {
        if (limit != null && limit > 0 && limit <= 50) {
            return limit;
        }
        return 10;
    }

    private <E, D> CursorPageResponse<D> toCursorResponse(
            List<E> entities,
            int limit,
            Function<E, D> mapper,
            Function<D, Long> idExtractor
    ) {
        boolean hasNext = entities.size() > limit;
        if (hasNext) {
            entities = entities.subList(0, limit);
        }

        List<D> dtoList = entities.stream()
                .map(mapper)
                .collect(Collectors.toList());

        String nextCursor = null;
        if (hasNext && !dtoList.isEmpty()) {
            Long lastId = idExtractor.apply(dtoList.get(dtoList.size() - 1));
            nextCursor = lastId.toString();
        }

        return new CursorPageResponse<>(
                dtoList,
                nextCursor,
                dtoList.size(),
                hasNext
        );
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
