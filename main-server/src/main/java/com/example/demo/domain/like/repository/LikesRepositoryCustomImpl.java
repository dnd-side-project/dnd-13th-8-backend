package com.example.demo.domain.like.repository;

import com.example.demo.domain.playlist.dto.common.PlaylistSortOption;
import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.playlist.entity.QPlaylist;
import com.example.demo.domain.user.entity.QUsers;
import com.example.demo.domain.like.entity.QLikes;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class LikesRepositoryCustomImpl implements LikesRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Long> findLikedPlaylistIdsIn(String userId, List<Long> playlistIds) {
        if (playlistIds == null || playlistIds.isEmpty()) return List.of();

        QLikes l = QLikes.likes;

        return queryFactory
                .select(l.playlist.id)
                .from(l)
                .where(
                        l.users.id.eq(userId),
                        l.playlist.id.in(playlistIds)
                )
                .fetch();
    }

    @Override
    public List<Playlist> findLikedPlaylistsWithMeta(String userId,
                                                     PlaylistSortOption sort,
                                                     int limit) {
        QLikes l = QLikes.likes;
        QPlaylist p = QPlaylist.playlist;
        QUsers u = QUsers.users; // playlist owner

        OrderSpecifier<?> order = switch (sort) {
            case POPULAR -> p.visitCount.desc();
            case RECENT  -> p.createdAt.desc();
        };

        return queryFactory
                .selectFrom(p)
                .join(l).on(l.playlist.id.eq(p.id))
                .join(p.users, u).fetchJoin()
                .where(
                        l.users.id.eq(userId)
                )
                .orderBy(order)
                .limit(limit)
                .distinct()
                .fetch();
    }

    @Override
    public long countLikedPlaylists(String userId, boolean includePrivate) {
        QLikes l = QLikes.likes;
        QPlaylist p = QPlaylist.playlist;

        BooleanExpression visibilityCondition = includePrivate
                ? null
                : p.isPublic.isTrue();

        Long count = queryFactory
                .select(p.id.countDistinct())
                .from(l)
                .join(l.playlist, p)
                .where(
                        l.users.id.eq(userId),
                        visibilityCondition
                )
                .fetchOne();

        return count != null ? count : 0L;
    }
}
