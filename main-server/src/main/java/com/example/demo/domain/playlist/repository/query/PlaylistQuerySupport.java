package com.example.demo.domain.playlist.repository.query;

import com.example.demo.domain.playlist.dto.common.PlaylistSortOption;
import com.example.demo.domain.playlist.dto.feed.CarouselDirection;
import com.example.demo.domain.playlist.dto.feed.PlaylistCursor;
import com.example.demo.domain.playlist.entity.QPlaylist;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.springframework.stereotype.Component;

@Component
public class PlaylistQuerySupport {

    public OrderSpecifier<?>[] orderBy(QPlaylist p, PlaylistSortOption sort) {
        return switch (sort) {
            case RECENT -> new OrderSpecifier<?>[]{p.id.desc()};
            case POPULAR -> new OrderSpecifier<?>[]{p.visitCount.desc(), p.id.desc()};
        };
    }

    public OrderSpecifier<?>[] orderByDirectional(
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

    public BooleanExpression visibilityCondition(QPlaylist p, boolean includePrivate) {
        return includePrivate ? null : p.isPublic.isTrue();
    }

    public BooleanExpression cursorCondition(
            QPlaylist p,
            PlaylistCursor cursor,
            PlaylistSortOption sort
    ) {
        if (cursor == null) return null;

        return switch (sort) {
            case RECENT -> cursor.id() == null
                    ? null
                    : p.id.lt(cursor.id());

            case POPULAR -> {
                if (cursor.visitCount() == null || cursor.id() == null) yield null;

                yield p.visitCount.lt(cursor.visitCount())
                        .or(p.visitCount.eq(cursor.visitCount()).and(p.id.lt(cursor.id())));
            }
        };
    }

    public BooleanExpression cursorConditionDirectional(
            QPlaylist p,
            PlaylistCursor cursor,
            PlaylistSortOption sort,
            CarouselDirection direction
    ) {
        if (cursor == null) return null;

        return switch (sort) {
            case RECENT -> {
                if (cursor.id() == null) yield null;

                yield direction == CarouselDirection.NEXT
                        ? p.id.lt(cursor.id())
                        : p.id.gt(cursor.id());
            }

            case POPULAR -> {
                if (cursor.visitCount() == null || cursor.id() == null) yield null;

                if (direction == CarouselDirection.NEXT) {
                    yield p.visitCount.lt(cursor.visitCount())
                            .or(p.visitCount.eq(cursor.visitCount()).and(p.id.lt(cursor.id())));
                }

                yield p.visitCount.gt(cursor.visitCount())
                        .or(p.visitCount.eq(cursor.visitCount()).and(p.id.gt(cursor.id())));
            }
        };
    }
}
