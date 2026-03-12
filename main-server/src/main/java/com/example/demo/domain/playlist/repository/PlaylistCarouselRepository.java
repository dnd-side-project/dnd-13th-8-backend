package com.example.demo.domain.playlist.repository;

import com.example.demo.domain.like.entity.QLikes;
import com.example.demo.domain.playlist.dto.common.PlaylistSortOption;
import com.example.demo.domain.playlist.dto.feed.CarouselDirection;
import com.example.demo.domain.playlist.dto.feed.PlaylistCursor;
import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.playlist.entity.QPlaylist;
import com.example.demo.domain.user.entity.QUsers;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PlaylistCarouselRepository {

    private final JPAQueryFactory queryFactory;

    private OrderSpecifier<?>[] orderByDirectional(
            QPlaylist p,
            PlaylistSortOption sort,
            CarouselDirection direction
    ) {
        boolean next = direction == CarouselDirection.NEXT;

        return switch (sort) {
            case RECENT -> next
                    ? new OrderSpecifier<?>[]{p.id.desc()}
                    : new OrderSpecifier<?>[]{p.id.asc()};

            case POPULAR -> next
                    ? new OrderSpecifier<?>[]{p.visitCount.desc(), p.id.desc()}
                    : new OrderSpecifier<?>[]{p.visitCount.asc(), p.id.asc()};
        };
    }

    private BooleanExpression visibilityCondition(QPlaylist p, boolean includePrivate) {
        if (includePrivate) {
            return null;
        }
        return p.isPublic.isTrue();
    }

    private BooleanExpression cursorConditionDirectional(
            QPlaylist p,
            PlaylistCursor cursor,
            PlaylistSortOption sort,
            CarouselDirection direction
    ) {
        if (cursor == null) {
            return null;
        }

        return switch (sort) {
            case RECENT -> {
                if (cursor.id() == null) {
                    yield null;
                }

                yield direction == CarouselDirection.NEXT
                        ? p.id.lt(cursor.id())
                        : p.id.gt(cursor.id());
            }

            case POPULAR -> {
                if (cursor.visitCount() == null || cursor.id() == null) {
                    yield null;
                }

                if (direction == CarouselDirection.NEXT) {
                    yield p.visitCount.lt(cursor.visitCount())
                            .or(p.visitCount.eq(cursor.visitCount()).and(p.id.lt(cursor.id())));
                } else {
                    yield p.visitCount.gt(cursor.visitCount())
                            .or(p.visitCount.eq(cursor.visitCount()).and(p.id.gt(cursor.id())));
                }
            }
        };
    }

    private JPAQuery<Playlist> feedCarouselBaseQuery(
            String ownerUserId,
            boolean includePrivate
    ) {
        QPlaylist p = QPlaylist.playlist;
        QUsers u = QUsers.users;

        return queryFactory
                .selectFrom(p)
                .join(p.users, u).fetchJoin()
                .where(
                        u.id.eq(ownerUserId),
                        visibilityCondition(p, includePrivate)
                );
    }

    private JPAQuery<Playlist> likedCarouselBaseQuery(
            String userId,
            boolean includePrivate
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
                        visibilityCondition(p, includePrivate)
                )
                .distinct();
    }

    public Optional<Playlist> findFeedAnchor(
            String ownerUserId,
            Long anchorId,
            boolean includePrivate
    ) {
        QPlaylist p = QPlaylist.playlist;

        return Optional.ofNullable(
                feedCarouselBaseQuery(ownerUserId, includePrivate)
                        .where(p.id.eq(anchorId))
                        .fetchOne()
        );
    }

    public Optional<Playlist> findLikedAnchor(
            String userId,
            Long anchorId,
            boolean includePrivate
    ) {
        QPlaylist p = QPlaylist.playlist;

        return Optional.ofNullable(
                likedCarouselBaseQuery(userId, includePrivate)
                        .where(p.id.eq(anchorId))
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

        return feedCarouselBaseQuery(ownerUserId, includePrivate)
                .where(cursorConditionDirectional(p, cursor, sort, direction))
                .orderBy(orderByDirectional(p, sort, direction))
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
        QPlaylist p = QPlaylist.playlist;

        return likedCarouselBaseQuery(userId, includePrivate)
                .where(cursorConditionDirectional(p, cursor, sort, direction))
                .orderBy(orderByDirectional(p, sort, direction))
                .limit(limit + 1L)
                .fetch();
    }
}
