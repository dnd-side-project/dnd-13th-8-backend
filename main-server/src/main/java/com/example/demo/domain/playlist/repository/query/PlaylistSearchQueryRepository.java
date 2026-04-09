package com.example.demo.domain.playlist.repository.query;

import com.example.demo.domain.cd.dto.response.CdResponse;
import com.example.demo.domain.playlist.dto.common.PlaylistGenre;
import com.example.demo.domain.playlist.dto.common.PlaylistSortOption;
import com.example.demo.domain.playlist.dto.feed.PlaylistCursor;
import com.example.demo.domain.playlist.dto.search.PlaylistSearchDto;
import com.example.demo.domain.playlist.dto.search.SearchResult;
import com.example.demo.domain.playlist.dto.search.SearchType;
import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.playlist.entity.QPlaylist;
import com.example.demo.domain.user.entity.QUsers;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.querydsl.core.types.dsl.Expressions.nullExpression;

@Repository
@RequiredArgsConstructor
public class PlaylistSearchQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final PlaylistQuerySupport querySupport;

    public SearchResult<Playlist> findByGenreWithCursor(
            PlaylistGenre genre,
            PlaylistSortOption sort,
            PlaylistCursor cursor,
            int limit
    ) {
        QPlaylist p = QPlaylist.playlist;
        QUsers u = QUsers.users;

        List<Playlist> results = queryFactory
                .selectFrom(p)
                .join(p.users, u).fetchJoin()
                .where(
                        p.genre.eq(genre),
                        p.isPublic.isTrue(),
                        querySupport.cursorCondition(p, cursor, sort)
                )
                .orderBy(querySupport.orderBy(p, sort))
                .limit(limit + 1L)
                .fetch();

        long totalCount = Optional.ofNullable(
                queryFactory
                        .select(p.count())
                        .from(p)
                        .where(
                                p.genre.eq(genre),
                                p.isPublic.isTrue()
                        )
                        .fetchOne()
        ).orElse(0L);

        return new SearchResult<>(results, totalCount);
    }

    public long countPlaylistByTitle(String query) {
        QPlaylist p = QPlaylist.playlist;

        BooleanBuilder builder = new BooleanBuilder()
                .and(p.name.containsIgnoreCase(query))
                .and(p.isPublic.isTrue());

        return Optional.ofNullable(
                queryFactory
                        .select(p.id.count())
                        .from(p)
                        .where(builder)
                        .fetchOne()
        ).orElse(0L);
    }

    public SearchResult<PlaylistSearchDto> searchPlaylistsByTitleWithOffset(
            String query,
            PlaylistSortOption sort,
            int offset,
            int limit
    ) {
        QPlaylist p = QPlaylist.playlist;
        QUsers u = QUsers.users;

        BooleanBuilder builder = new BooleanBuilder()
                .and(p.name.containsIgnoreCase(query))
                .and(p.isPublic.isTrue());

        List<PlaylistSearchDto> results = queryFactory
                .select(Projections.constructor(
                        PlaylistSearchDto.class,
                        Expressions.constant(SearchType.PLAYLIST),
                        p.id,
                        p.name,
                        u.id,
                        u.username,
                        nullExpression(CdResponse.class)
                ))
                .from(p)
                .join(p.users, u)
                .where(builder)
                .orderBy(querySupport.orderBy(p, sort))
                .offset(offset)
                .limit(limit)
                .fetch();

        long totalCount = Optional.ofNullable(
                queryFactory
                        .select(p.id.count())
                        .from(p)
                        .where(builder)
                        .fetchOne()
        ).orElse(0L);

        return new SearchResult<>(results, totalCount);
    }
}
