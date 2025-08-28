package com.example.demo.domain.user.repository;

import com.example.demo.domain.playlist.dto.PlaylistSortOption;
import com.example.demo.domain.playlist.dto.search.SearchResult;
import com.example.demo.domain.playlist.dto.search.UserSearchDto;
import com.example.demo.domain.representative.entity.QRepresentativePlaylist;
import com.example.demo.domain.user.entity.QUsers;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserRepositoryCustomImpl implements UserRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public SearchResult<UserSearchDto> searchUsersByQueryWithOffset(
            String query,
            PlaylistSortOption sort,
            int offset,
            int limit
    ) {
        QUsers u = QUsers.users;
        QRepresentativePlaylist rp = QRepresentativePlaylist.representativePlaylist;

        BooleanBuilder condition = new BooleanBuilder()
                .and(u.username.containsIgnoreCase(query));

        // 정렬 조건
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();
        if (sort == PlaylistSortOption.POPULAR) {
            orderSpecifiers.add(rp.playlist.visitCount.desc());
        } else {
            orderSpecifiers.add(rp.playlist.createdAt.desc());
        }
        orderSpecifiers.add(rp.id.desc());

        //  1. 결과 목록 조회
        List<UserSearchDto> results = queryFactory
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

        //  2. 총 유저 수 조회 (중복 제거)
        long totalCount = Optional.ofNullable(
                queryFactory
                        .select(u.id.countDistinct())
                        .from(rp)
                        .join(rp.user, u)
                        .where(condition)
                        .fetchOne()
        ).orElse(0L);


        //  3. SearchResult로 묶어서 반환
        return new SearchResult<>(results, totalCount);
    }

}
