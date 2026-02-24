package com.example.demo.domain.playlist.repository;

import com.example.demo.domain.like.entity.QLikes;
import com.example.demo.domain.playlist.dto.common.PlaylistSortOption;
import com.example.demo.domain.playlist.dto.feed.PlaylistCursor;
import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.playlist.entity.QPlaylist;
import com.example.demo.domain.user.entity.QUsers;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PlaylistFeedRepository {

    private final JPAQueryFactory queryFactory;

    private OrderSpecifier<?>[] orderBy(QPlaylist p, PlaylistSortOption sort) {
        return switch (sort) {
            case RECENT -> new OrderSpecifier<?>[]{ p.id.desc() };
            case POPULAR -> new OrderSpecifier<?>[]{ p.visitCount.desc(), p.id.desc() };
        };
    }

    private BooleanExpression cursorCondition(QPlaylist p,
                                              PlaylistCursor cursor,
                                              PlaylistSortOption sort) {
        if (cursor == null) return null;

        return switch (sort) {
            case RECENT -> cursor.id() == null
                    ? null
                    : p.id.lt(cursor.id());

            case POPULAR -> {
                if (cursor.visitCount() == null || cursor.id() == null) yield null;

                yield p.visitCount.lt(cursor.visitCount())
                        .or(
                                p.visitCount.eq(cursor.visitCount())
                                        .and(p.id.lt(cursor.id()))
                        );
            }
        };
    }

    private BooleanExpression visibilityCondition(QPlaylist p,
                                                  boolean includePrivate) {
        if (includePrivate) return null;
        return p.isPublic.isTrue();
    }

    public List<Playlist> findFeedPlaylists(
            String ownerUserId,
            PlaylistCursor cursor,
            int limit,
            PlaylistSortOption sort,
            boolean includePrivate
    ) {
        QPlaylist p = QPlaylist.playlist;
        QUsers u = QUsers.users;

        return queryFactory
                .selectFrom(p)
                .join(p.users, u).fetchJoin()
                .where(
                        u.id.eq(ownerUserId),
                        visibilityCondition(p, includePrivate),
                        cursorCondition(p, cursor, sort)
                )
                .orderBy(orderBy(p, sort))
                .limit(limit + 1L)
                .fetch();
    }

    public List<Playlist> findLikedPlaylists(
            String userId,
            PlaylistSortOption sort,
            PlaylistCursor cursor,
            int limit,
            boolean includePrivate
    ) {
        QLikes l = QLikes.likes;
        QPlaylist p = QPlaylist.playlist;
        QUsers u = QUsers.users;

        return queryFactory
                .selectFrom(p)
                .join(l).on(l.playlist.id.eq(p.id))
                .join(p.users, u).fetchJoin()
                .where(
                        l.users.id.eq(userId),
                        visibilityCondition(p, includePrivate),
                        cursorCondition(p, cursor, sort)
                )
                .orderBy(orderBy(p, sort))
                .limit(limit + 1L)
                .distinct()
                .fetch();
    }
}