package com.example.demo.domain.playlist.repository.query;

import com.example.demo.domain.like.entity.QLikes;
import com.example.demo.domain.playlist.dto.common.PlaylistSortOption;
import com.example.demo.domain.playlist.dto.feed.PlaylistCursor;
import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.playlist.entity.QPlaylist;
import com.example.demo.domain.user.entity.QUsers;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PlaylistFeedQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final PlaylistQuerySupport querySupport;

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
                        querySupport.visibilityCondition(p, includePrivate),
                        querySupport.cursorCondition(p, cursor, sort)
                )
                .orderBy(querySupport.orderBy(p, sort))
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
                        querySupport.visibilityCondition(p, includePrivate),
                        querySupport.cursorCondition(p, cursor, sort)
                )
                .orderBy(querySupport.orderBy(p, sort))
                .limit(limit + 1L)
                .distinct()
                .fetch();
    }

    public long countFeedPlaylists(String ownerId, boolean includePrivate) {
        QPlaylist p = QPlaylist.playlist;

        Long count = queryFactory
                .select(p.count())
                .from(p)
                .where(
                        p.users.id.eq(ownerId),
                        querySupport.visibilityCondition(p, includePrivate)
                )
                .fetchOne();

        return count != null ? count : 0L;
    }
}