package com.example.demo.domain.playlist.repository.query;

import com.example.demo.domain.like.entity.QLikes;
import com.example.demo.domain.playlist.dto.common.PlaylistSortOption;
import com.example.demo.domain.playlist.dto.feed.CarouselDirection;
import com.example.demo.domain.playlist.dto.feed.PlaylistCursor;
import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.playlist.entity.QPlaylist;
import com.example.demo.domain.user.entity.QUsers;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PlaylistCarouselQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final PlaylistQuerySupport querySupport;

    public Optional<Playlist> findFeedAnchor(
            String ownerUserId,
            Long anchorId,
            boolean includePrivate
    ) {
        QPlaylist p = QPlaylist.playlist;
        QUsers u = QUsers.users;

        return Optional.ofNullable(
                queryFactory
                        .selectFrom(p)
                        .join(p.users, u).fetchJoin()
                        .where(
                                u.id.eq(ownerUserId),
                                p.id.eq(anchorId),
                                querySupport.visibilityCondition(p, includePrivate)
                        )
                        .fetchOne()
        );
    }

    public Optional<Playlist> findLikedAnchor(
            String userId,
            Long anchorId,
            boolean includePrivate
    ) {
        QLikes l = QLikes.likes;
        QPlaylist p = QPlaylist.playlist;
        QUsers u = QUsers.users;

        return Optional.ofNullable(
                queryFactory
                        .select(p)
                        .from(l)
                        .join(l.playlist, p)
                        .join(p.users, u).fetchJoin()
                        .where(
                                l.users.id.eq(userId),
                                p.id.eq(anchorId),
                                querySupport.visibilityCondition(p, includePrivate)
                        )
                        .distinct()
                        .fetchOne()
        );
    }

    public List<Playlist> findFeedCarousel(
            String ownerUserId,
            PlaylistCursor cursor,
            int limit,
            PlaylistSortOption sort,
            boolean includePrivate,
            CarouselDirection direction
    ) {
        QPlaylist p = QPlaylist.playlist;
        QUsers u = QUsers.users;

        return queryFactory
                .selectFrom(p)
                .join(p.users, u).fetchJoin()
                .where(
                        u.id.eq(ownerUserId),
                        querySupport.visibilityCondition(p, includePrivate),
                        querySupport.cursorConditionDirectional(p, cursor, sort, direction)
                )
                .orderBy(querySupport.orderByDirectional(p, sort, direction))
                .limit(limit + 1L)
                .fetch();
    }

    public List<Playlist> findLikedCarousel(
            String userId,
            PlaylistCursor cursor,
            int limit,
            PlaylistSortOption sort,
            boolean includePrivate,
            CarouselDirection direction
    ) {
        QLikes l = QLikes.likes;
        QPlaylist p = QPlaylist.playlist;
        QUsers u = QUsers.users;

        return queryFactory
                .select(p)
                .from(l)
                .join(l.playlist, p)
                .join(p.users, u).fetchJoin()
                .where(
                        l.users.id.eq(userId),
                        querySupport.visibilityCondition(p, includePrivate),
                        querySupport.cursorConditionDirectional(p, cursor, sort, direction)
                )
                .orderBy(querySupport.orderByDirectional(p, sort, direction))
                .limit(limit + 1L)
                .distinct()
                .fetch();
    }
}
