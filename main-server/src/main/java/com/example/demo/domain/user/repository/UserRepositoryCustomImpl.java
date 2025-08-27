package com.example.demo.domain.user.repository;

import com.example.demo.domain.playlist.dto.PlaylistSortOption;
import com.example.demo.domain.playlist.dto.search.UserSearchDto;
import com.example.demo.domain.representative.entity.QRepresentativePlaylist;
import com.example.demo.domain.song.entity.QSong;
import com.example.demo.domain.user.entity.QUsers;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserRepositoryCustomImpl implements UserRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<UserSearchDto> searchUsersByQueryWithOffset(String query, PlaylistSortOption sort, int offset, int limit) {
        QUsers u = QUsers.users;
        QRepresentativePlaylist rp = QRepresentativePlaylist.representativePlaylist;

        BooleanBuilder condition = new BooleanBuilder()
                .and(u.username.containsIgnoreCase(query));

        // 정렬 조건 설정
        List<com.querydsl.core.types.OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();
        if (sort == PlaylistSortOption.POPULAR) {
            orderSpecifiers.add(rp.playlist.visitCount.desc());
        } else { // RECENT 또는 기본
            orderSpecifiers.add(rp.playlist.createdAt.desc());
        }
        orderSpecifiers.add(rp.id.desc()); // 항상 tie-breaker

        return queryFactory
                .select(Projections.constructor(
                        UserSearchDto.class,
                        u.id,
                        u.username,
                        u.profileUrl,
                        rp.id,
                        rp.playlist.name
                ))
                .from(rp)
                .join(rp.user, u)
                .where(condition)
                .orderBy(orderSpecifiers.toArray(new OrderSpecifier[0]))
                .offset(offset)
                .limit(limit)
                .fetch();
    }


}
