package com.example.demo.domain.follow.repository;

import com.example.demo.domain.follow.dto.FollowPlaylistDto;
import com.example.demo.domain.follow.entity.QFollow;
import com.example.demo.domain.playlist.dto.PlaylistSortOption;
import com.example.demo.domain.playlist.entity.QPlaylist;
import com.example.demo.domain.user.entity.QUsers;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FollowRepositoryCustomImpl implements FollowRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public List<FollowPlaylistDto> findFolloweePlaylistsWithMeta(String userId, PlaylistSortOption sort, int limit) {
        QFollow f = QFollow.follow;
        QPlaylist p = QPlaylist.playlist;
        QUsers u = QUsers.users;

        NumberExpression<Long> visitCountExpr = p.visitCount;

        OrderSpecifier<?> order = switch (sort) {
            case POPULAR -> visitCountExpr.desc();
            case RECENT -> p.id.desc();
        };

        return queryFactory
                .select(Projections.constructor(
                        FollowPlaylistDto.class,
                        p.visitCount.intValue(),        // 플레이리스트 조회수 총합 (또는 개수)
                        p.users.id,         // 제작자 ID
                        p.users.username,  // 제작자 닉네임
                        p.users.profileUrl
                ))
                .from(f)
                .join(f.playlist, p)
                .join(p.users, u)
                .where(f.users.id.eq(userId))
                .groupBy(p.users.id, p.users.username)
                .orderBy(order)
                .limit(limit)
                .fetch();
    }

}
