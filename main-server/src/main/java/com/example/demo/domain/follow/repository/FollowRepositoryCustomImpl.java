package com.example.demo.domain.follow.repository;

import com.example.demo.domain.follow.dto.response.FollowListItem;
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

    // 해당 유저를 팔로우하는 사람 목록
    @Override
    public List<FollowListItem> findFollowerListByUserId(String userId, Long cursor, int limit) {
        QFollow f = QFollow.follow;
        QUsers u = QUsers.users;

        return queryFactory
                .select(Projections.constructor(
                        FollowListItem.class,
                        f.id,
                        u.id.stringValue(),
                        u.username,
                        u.shareCode,
                        u.profileUrl
                ))
                .from(f)
                .join(f.follower, u)
                .where(
                        f.followee.id.eq(userId),
                        cursor != null ? f.id.lt(cursor) : null
                )
                .orderBy(f.id.desc())
                .limit(limit+1)
                .fetch();
    }

    @Override
    public List<FollowListItem> findFolloweeListByUserId(String userId, Long cursor, int limit) {
        QFollow f = QFollow.follow;
        QUsers u = QUsers.users;

        return queryFactory
                .select(Projections.constructor(
                        FollowListItem.class,
                        f.id,
                        u.id.stringValue(),
                        u.username,
                        u.shareCode,
                        u.profileUrl
                ))
                .from(f)
                .join(f.followee, u)
                .where(
                        f.follower.id.eq(userId),
                        cursor != null ? f.id.lt(cursor) : null
                )
                .orderBy(f.id.desc())
                .limit(limit + 1)
                .fetch();
    }

    // 목록 중에 내가 팔로우 중인 아이디만 가져옴
    @Override
    public List<String> findFolloweeIdsIn(String userId, List<String> followeeIds) {
        if (followeeIds == null || followeeIds.isEmpty()) return List.of();

        QFollow f = QFollow.follow;

        return queryFactory
                .select(f.followee.id)
                .from(f)
                .where(
                        f.follower.id.eq(userId),
                        f.followee.id.in(followeeIds)
                )
                .fetch();
    }
}
