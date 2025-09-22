package com.example.demo.domain.follow.repository;

import com.example.demo.domain.follow.dto.response.FollowedPlaylist;
import com.example.demo.domain.follow.entity.QFollow;
import com.example.demo.domain.playlist.dto.PlaylistSortOption;
import com.example.demo.domain.playlist.entity.QPlaylist;
import com.example.demo.domain.representative.entity.QRepresentativePlaylist;
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
        QUsers u = QUsers.users; // followee(피팔로우) 유저
        QRepresentativePlaylist r = QRepresentativePlaylist.representativePlaylist;
        QPlaylist p = QPlaylist.playlist;

        OrderSpecifier<?> order = switch (sort) {
            case POPULAR -> p.visitCount.desc();
            case RECENT  -> p.createdAt.desc();
        };

        return queryFactory
                .select(Projections.constructor(
                        FollowedPlaylist.class,
                        u.id.stringValue(),      // String creatorId (followee의 id)
                        p.id.stringValue(),      // String creatorPlaylistId (대표 플레이리스트 id)
                        u.username,              // String creatorNickname
                        u.profileUrl             // String creatorProfileImageUrl
                ))
                .from(f)
                .join(f.followee, u)                         // 팔로우 대상 유저
                .leftJoin(r).on(r.user.id.eq(u.id))         // 해당 유저의 대표플레이리스트
                .leftJoin(r.playlist, p)                    // 대표플리 엔티티 조인
                .where(f.follower.id.eq(followerId))        // 기준: 내가(followerId) 팔로우한 대상들
                .orderBy(order)
                .limit(limit)
                .fetch();
    }
}
