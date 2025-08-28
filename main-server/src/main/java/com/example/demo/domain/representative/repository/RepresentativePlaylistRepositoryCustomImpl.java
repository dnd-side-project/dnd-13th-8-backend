package com.example.demo.domain.representative.repository;

import static com.querydsl.core.types.dsl.Expressions.nullExpression;

import com.example.demo.domain.cd.dto.response.OnlyCdResponse;
import com.example.demo.domain.playlist.dto.PlaylistGenre;
import com.example.demo.domain.playlist.dto.PlaylistSortOption;
import com.example.demo.domain.playlist.dto.search.PlaylistSearchDto;
import com.example.demo.domain.playlist.dto.search.SearchResult;
import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.playlist.entity.QPlaylist;
import com.example.demo.domain.representative.entity.QRepresentativePlaylist;
import com.example.demo.domain.representative.entity.RepresentativePlaylist;
import com.example.demo.domain.user.entity.QUsers;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.*;

@RequiredArgsConstructor
public class RepresentativePlaylistRepositoryCustomImpl implements RepresentativePlaylistRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QPlaylist playlist = QPlaylist.playlist;

    @Override
    public List<Playlist> findByVisitCount(int limit) {
        QRepresentativePlaylist rp = QRepresentativePlaylist.representativePlaylist;
        QUsers u = QUsers.users;

        return queryFactory
                .select(rp)
                .from(rp)
                .join(rp.playlist, playlist).fetchJoin()
                .join(playlist.users, u).fetchJoin()
                .orderBy(playlist.visitCount.desc())
                .limit(limit)
                .fetch()
                .stream()
                .map(RepresentativePlaylist::getPlaylist)
                .toList();
    }
    @Override
    public SearchResult<PlaylistSearchDto> searchPlaylistsByTitleWithOffset(
            String query,
            PlaylistSortOption sort,
            int offset,
            int limit
    ) {
        QPlaylist p = QPlaylist.playlist;
        QUsers u = QUsers.users;

        BooleanBuilder builder = new BooleanBuilder()
                .and(p.name.containsIgnoreCase(query));

        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();
        if (sort == PlaylistSortOption.POPULAR) {
            orderSpecifiers.add(p.visitCount.desc());
        } else {
            orderSpecifiers.add(p.createdAt.desc());
        }
        orderSpecifiers.add(p.id.desc());

        //  1. 목록 조회
        List<PlaylistSearchDto> results = queryFactory
                .select(Projections.constructor(
                        PlaylistSearchDto.class,
                        p.id,
                        p.name,
                        u.id,
                        u.username,
                        nullExpression(OnlyCdResponse.class)
                ))
                .from(p)
                .join(p.users, u)
                .where(builder)
                .orderBy(orderSpecifiers.toArray(new OrderSpecifier[0]))
                .offset(offset)
                .limit(limit)
                .fetch();

        // 2. 총 개수 조회 (중복 제거)
        long totalCount = Optional.ofNullable(queryFactory
                .select(p.id.countDistinct())
                .from(p)
                .where(builder)
                .fetchOne()
        ).orElse(0L);

        //  3. 결과 반환
        return new SearchResult<>(results, totalCount);
    }

    @Override
    public SearchResult<RepresentativePlaylist> findByGenreWithCursor(
            PlaylistGenre genre,
            PlaylistSortOption sort,
            Long cursorId,
            int limit
    ) {
        QRepresentativePlaylist rp = QRepresentativePlaylist.representativePlaylist;
        QPlaylist p = QPlaylist.playlist;

        // WHERE 조건
        BooleanBuilder builder = new BooleanBuilder()
                .and(p.genre.eq(genre));
        if (cursorId != null && cursorId > 0) {
            builder.and(rp.id.lt(cursorId));
        }

        // 정렬 조건
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();
        if (sort == PlaylistSortOption.POPULAR) {
            orderSpecifiers.add(p.visitCount.desc());
        } else {
            orderSpecifiers.add(p.createdAt.desc());
        }
        orderSpecifiers.add(rp.id.desc()); // tie-breaker

        // limit + 1 조회 (슬라이싱은 밖에서 함)
        List<RepresentativePlaylist> results = queryFactory
                .selectFrom(rp)
                .join(rp.playlist, p).fetchJoin()
                .where(builder)
                .orderBy(orderSpecifiers.toArray(new OrderSpecifier[0]))
                .limit(limit + 1)
                .fetch();

        // 총 개수 (중복 제거)
        Long countResult = queryFactory
                .select(p.id.countDistinct())
                .from(rp)
                .join(rp.playlist, p)
                .where(builder)
                .fetchOne();
        long totalCount = (countResult != null) ? countResult : 0L;

        return new SearchResult<>(results, totalCount);
    }
}
