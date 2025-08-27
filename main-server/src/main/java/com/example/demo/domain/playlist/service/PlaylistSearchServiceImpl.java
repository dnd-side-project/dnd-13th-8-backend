package com.example.demo.domain.playlist.service;

import com.example.demo.domain.playlist.dto.PlaylistGenre;
import com.example.demo.domain.playlist.dto.search.PlaylistSearchResponse;
import com.example.demo.domain.playlist.dto.PlaylistSortOption;
import com.example.demo.domain.playlist.dto.search.CombinedSearchResponse;
import com.example.demo.domain.playlist.dto.search.PlaylistSearchDto;
import com.example.demo.domain.playlist.dto.search.PopularItem;
import com.example.demo.domain.playlist.dto.search.SearchItem;
import com.example.demo.domain.playlist.dto.search.UserSearchDto;
import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.representative.entity.RepresentativePlaylist;
import com.example.demo.domain.representative.repository.RepresentativeRepresentativePlaylistRepository;
import java.text.Normalizer;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlaylistSearchServiceImpl implements PlaylistSearchService {

    private final RepresentativeRepresentativePlaylistRepository representativePlaylistRepository;
    private final StringRedisTemplate redis;

    // 기본 인기 검색어 (Redis 데이터가 없을 때 fallback)
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
    public List<PlaylistSearchResponse> searchByGenre(PlaylistGenre genre, PlaylistSortOption sort, Integer limit) {
        int finalLimit = 10;
        if (limit != null && limit > 0 && limit <= 50) {
            finalLimit = limit;
        }

        Pageable pageable = PageRequest.of(0, finalLimit);

        List<RepresentativePlaylist> representatives = switch (sort) {
            case POPULAR -> representativePlaylistRepository
                    .findByGenreOrderByVisitCountDesc(genre, pageable);
            case RECENT -> representativePlaylistRepository
                    .findByGenreOrderByCreatedAtDesc(genre, pageable);
        };

        return representatives.stream()
                .map(rep -> {
                    Playlist p = rep.getPlaylist();
                    return new PlaylistSearchResponse(
                            p.getId(),
                            p.getName(),
                            p.getUsers().getId(),
                            p.getUsers().getUsername()
                    );
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CombinedSearchResponse searchAll(String query, PlaylistSortOption sort, int limit) {
        recordSearchTerm(query);

        Pageable pageable = Pageable.ofSize(limit);

        List<PlaylistSearchDto> playlists = representativePlaylistRepository
                .searchRepresentativePlaylists(query, sort, pageable);

        List<UserSearchDto> users = representativePlaylistRepository
                .searchUsersWithRepresentativePlaylist(query, pageable);

        List<SearchItem> combined = new ArrayList<>();
        combined.addAll(users);      // type = "USER"
        combined.addAll(playlists);  // type = "PLAYLIST"

        return new CombinedSearchResponse(combined);
    }


    @Override
    public List<PopularItem> getPopularTerms(String range, int limit) {
        String redisKey = resolveKeyFromRange(range);

        // Redis ZSET에서 인기 검색어 가져오기
        Set<TypedTuple<String>> raw =
                redis.opsForZSet().reverseRangeWithScores(redisKey, 0, limit - 1);

        // fallback 처리
        if (raw == null || raw.isEmpty()) {
            return DEFAULT_POPULAR_TERMS.stream().limit(limit).toList();
        }

        return raw.stream()
                .map(tuple -> new PopularItem(tuple.getValue()))
                .toList();
    }

    // range 값에 따른 Redis 키 생성
    private String resolveKeyFromRange(String range) {
        LocalDate now = LocalDate.now(ZoneId.of("Asia/Seoul"));
        return switch (range) {
            case "7d" -> "pop:term:" + now.minusDays(7) + ":" + now;  // 향후 확장 고려
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
        // 1. 앞뒤 공백 제거 → 2. 연속 공백 하나로 → 3. 유니코드 정규화 → 4. 소문자 변환
        return Normalizer.normalize(
                term.trim().replaceAll("\\s+", " "),
                Normalizer.Form.NFKC
        ).toLowerCase(Locale.ROOT);
    }

}


