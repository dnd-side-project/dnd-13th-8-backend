package com.example.demo.domain.follow.repository;

import com.example.demo.domain.follow.dto.FollowPlaylistDto;
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
    public List<FollowPlaylistDto> findFolloweePlaylistsWithMeta(String userId, PlaylistSortOption sort, int limit) {
        QFollow f = QFollow.follow;
        QUsers u = QUsers.users;
        QRepresentativePlaylist r = QRepresentativePlaylist.representativePlaylist;

        OrderSpecifier<?> order = switch (sort) {
            case POPULAR -> r.playlist.visitCount.desc();
            case RECENT -> r.playlist.createdAt.desc();
        };


        return queryFactory
                .select(Projections.constructor(
                        FollowPlaylistDto.class,
                        u.id.stringValue(),           // String creatorId
                        r.playlist.id.stringValue(),  // String creatorPlaylistId
                        u.username,                   // String creatorNickname
                        u.profileUrl                  // String creatorProfileImageUrl
                ))
                .from(f)
                .join(f.users, u)
                .join(r).on(r.user.id.eq(u.id))
                .where(f.users.id.eq(userId))
                .orderBy(order)
                .limit(limit)
                .fetch();
    }

}
