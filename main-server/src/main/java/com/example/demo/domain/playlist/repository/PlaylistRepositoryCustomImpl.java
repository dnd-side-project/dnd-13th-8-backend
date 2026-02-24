package com.example.demo.domain.playlist.repository;

import com.example.demo.domain.cd.dto.response.CdResponse;
import com.example.demo.domain.follow.entity.QFollow;
import com.example.demo.domain.playlist.dto.common.PlaylistGenre;
import com.example.demo.domain.playlist.dto.common.PlaylistSortOption;
import com.example.demo.domain.playlist.dto.search.PlaylistSearchDto;
import com.example.demo.domain.playlist.dto.search.SearchResult;
import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.playlist.entity.QPlaylist;
import com.example.demo.domain.song.entity.QSong;
import com.example.demo.domain.user.entity.QUsers;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;

import static com.querydsl.core.types.dsl.Expressions.nullExpression;

@RequiredArgsConstructor
public class PlaylistRepositoryCustomImpl implements PlaylistRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private OrderSpecifier<?>[] orderBy(QPlaylist p, PlaylistSortOption sort) {
        return switch (sort) {
            case RECENT -> new OrderSpecifier<?>[]{ p.id.desc() };
            case POPULAR -> new OrderSpecifier<?>[]{ p.visitCount.desc(), p.id.desc() };
        };
    }

    @Override
    public List<Playlist> findByUserIdSorted(String userId, PlaylistSortOption sort) {
        QPlaylist p = QPlaylist.playlist;
        QUsers u = QUsers.users;

        return queryFactory
                .selectFrom(p)
                .join(p.users, u).fetchJoin()
                .where(u.id.eq(userId))
                .orderBy(orderBy(p, sort))
                .fetch();
    }

    @Override
    public List<Long> findFollowedPlaylistIds(String currentUserId) {
        QFollow f = QFollow.follow;
        QPlaylist p = QPlaylist.playlist;
        QSong s = QSong.song;
        QUsers u = QUsers.users;

        return queryFactory
                .select(p.id)
                .from(p)
                .join(p.users, u)
                .join(s).on(s.playlist.id.eq(p.id))
                .where(p.isPublic.isTrue()
                        .and(u.id.in(JPAExpressions
                                        .select(f.followee.id)
                                        .from(f)
                                        .where(f.follower.id.eq(currentUserId))
                        ))
                )
                .groupBy(p.id)
                .having(s.count().goe(3))
                .fetch();
    }

    @Override
    public List<Playlist> findPlaylistsBySimilarSongs(List<Long> basePlaylistIds, String excludeUserId,
                                                      List<Long> excludePlaylistIds, int limit) {
        QSong s1 = new QSong("s1");
        QSong s2 = new QSong("s2");
        QSong sCount = new QSong("sCount");
        QPlaylist p2 = new QPlaylist("p2");
        QUsers u = QUsers.users;

        return queryFactory
                .select(p2)
                .distinct()
                .from(s1)
                .join(s2).on(
                        s1.youtubeUrl.eq(s2.youtubeUrl)
                                .or(s1.youtubeTitle.eq(s2.youtubeTitle))
                )
                .join(s2.playlist, p2)
                .join(p2.users, u).fetchJoin()
                .join(sCount).on(sCount.playlist.id.eq(p2.id))
                .where(
                        s1.playlist.id.in(basePlaylistIds),
                        p2.id.notIn(excludePlaylistIds),
                        u.id.ne(excludeUserId),
                        p2.isPublic.isTrue()
                )
                .groupBy(p2.id)
                .having(sCount.count().goe(3))
                .orderBy(p2.createdAt.desc())
                .limit(limit)
                .fetch();
    }

    @Override
    public SearchResult<Playlist> findByGenreWithCursor(
            PlaylistGenre genre,
            PlaylistSortOption sort,
            Long cursorId,
            int limit
    ) {
        QPlaylist p = QPlaylist.playlist;

        BooleanBuilder builder = new BooleanBuilder()
                .and(p.genre.eq(genre))
                .and(p.isPublic.isTrue());
        if (cursorId != null && cursorId > 0) {
            builder.and(p.id.lt(cursorId)); // 커서는 p.id 기준
        }

        // 쿼리를 변수로 받아서 orderBy를 개별 호출 (제네릭 유지)
        JPAQuery<Playlist> q = queryFactory
                .selectFrom(p)
                .where(builder);

        if (sort == PlaylistSortOption.POPULAR) {
            q.orderBy(p.visitCount.desc());
        } else {
            q.orderBy(p.createdAt.desc());
        }
        q.orderBy(p.id.desc()); // tie-breaker

        List<Playlist> results = q
                .limit(limit + 1) // 슬라이싱 용
                .fetch();

        long totalCount = Optional.ofNullable(
                queryFactory.select(p.id.count())
                        .from(p)
                        .where(builder)
                        .fetchOne()
        ).orElse(0L);

        return new SearchResult<>(results, totalCount);
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
                .and(p.name.containsIgnoreCase(query))
                .and(p.isPublic.isTrue());

        // 🔧 쿼리를 변수로 받아서 orderBy를 개별 호출 (제네릭 유지)
        JPAQuery<PlaylistSearchDto> q = queryFactory
                .select(Projections.constructor(
                        PlaylistSearchDto.class,
                        p.id,
                        p.name,
                        u.id,
                        u.username,
                        nullExpression(CdResponse.class)
                ))
                .from(p)
                .join(p.users, u)
                .where(builder);

        if (sort == PlaylistSortOption.POPULAR) {
            q.orderBy(p.visitCount.desc());
        } else {
            q.orderBy(p.createdAt.desc());
        }
        q.orderBy(p.id.desc()); // tie-breaker

        List<PlaylistSearchDto> results = q
                .offset(offset)
                .limit(limit)
                .fetch();

        long totalCount = Optional.ofNullable(
                queryFactory.select(p.id.count())
                        .from(p)
                        .where(builder)
                        .fetchOne()
        ).orElse(0L);

        return new SearchResult<>(results, totalCount);
    }

    @Override
    public List<Playlist> findByVisitCount(int limit) {
        QPlaylist p = QPlaylist.playlist;
        QUsers u = QUsers.users;

        // 이 메서드는 원래도 varargs 직접 호출이라 문제 없음
        return queryFactory
                .selectFrom(p)
                .join(p.users, u).fetchJoin()
                .where(p.isPublic.isTrue())
                .orderBy(p.visitCount.desc(), p.id.desc())
                .limit(limit)
                .fetch();
    }

    @Override
    public List<Playlist> findLatestPlaylists(String excludeUserId, List<Long> excludePlaylistIds,
                                              int limit) {
        QPlaylist p = QPlaylist.playlist;
        QUsers u = QUsers.users;
        QSong s = QSong.song;

        return queryFactory
                .select(p)
                .from(p)
                .join(p.users, u).fetchJoin()
                .join(s).on(s.playlist.id.eq(p.id))
                .where(
                        u.id.ne(excludeUserId),
                        p.id.notIn(excludePlaylistIds),
                        p.isPublic.isTrue()
                )
                .groupBy(p.id)
                .having(s.count().goe(3))
                .orderBy(p.createdAt.desc())
                .limit(limit)
                .fetch();
    }
}
