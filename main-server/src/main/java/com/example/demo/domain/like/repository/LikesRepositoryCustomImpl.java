package com.example.demo.domain.like.repository;

import com.example.demo.domain.playlist.dto.PlaylistSortOption;
import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.playlist.entity.QPlaylist;
import com.example.demo.domain.user.entity.QUsers;
import com.example.demo.domain.like.entity.QLikes;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class LikesRepositoryCustomImpl implements LikesRepositoryCustom {

    private final JPAQueryFactory queryFactory;

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
                        l.users.id.eq(userId),
                        p.isPublic.isTrue()
                )
                .orderBy(order)
                .limit(limit)
                .distinct()
                .fetch();
    }
}
