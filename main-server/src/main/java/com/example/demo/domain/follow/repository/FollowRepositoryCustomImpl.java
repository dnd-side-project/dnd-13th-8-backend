package com.example.demo.domain.follow.repository;

import com.example.demo.domain.follow.dto.response.FollowedPlaylist;
import com.example.demo.domain.follow.entity.QFollow;
import com.example.demo.domain.playlist.dto.common.PlaylistSortOption;
import com.example.demo.domain.playlist.entity.QPlaylist;
import com.example.demo.domain.user.entity.QUsers;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FollowRepositoryCustomImpl implements FollowRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<FollowedPlaylist> findFolloweePlaylistsWithMeta(String followerId,
                                                                PlaylistSortOption sort,
                                                                int limit) {
        QFollow f = QFollow.follow;
        QUsers u = QUsers.users; // followee
        QPlaylist p = QPlaylist.playlist;

        OrderSpecifier<?> order = switch (sort) {
            case POPULAR -> p.visitCount.desc();
            case RECENT  -> p.createdAt.desc();
        };

        return queryFactory
                .select(Projections.constructor(
                        FollowedPlaylist.class,
                        u.id.stringValue(),
                        p.id.stringValue(),
                        u.username,
                        u.profileUrl
                ))
                .from(p)
                .join(p.users, u)
                .join(f).on(f.followee.id.eq(u.id))
                .where(
                        f.follower.id.eq(followerId),
                        p.isPublic.isTrue()
                )
                .orderBy(order)
                .limit(limit)
                .fetch();
    }
}
