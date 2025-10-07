package com.example.demo.domain.user.repository;

import com.example.demo.domain.playlist.dto.PlaylistSortOption;
import com.example.demo.domain.playlist.dto.search.SearchResult;
import com.example.demo.domain.playlist.dto.search.UserSearchDto;
import com.example.demo.domain.user.entity.QUsers;
import com.example.demo.domain.playlist.entity.QPlaylist;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
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
        QPlaylist p = new QPlaylist("p");
        QPlaylist pSub = new QPlaylist("pSub");

        BooleanBuilder condition = new BooleanBuilder()
                .and(u.username.containsIgnoreCase(query));

        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();
        if (sort == PlaylistSortOption.POPULAR) {
            orderSpecifiers.add(p.visitCount.desc());
        } else {
            orderSpecifiers.add(p.createdAt.desc());
        }
        orderSpecifiers.add(p.id.desc());

        List<UserSearchDto> results = queryFactory
                .select(Projections.constructor(
                        UserSearchDto.class,
                        u.id,
                        u.username,
                        u.profileUrl,
                        p.id,
                        p.name
                ))
                .from(u)
                .leftJoin(p).on(
                        p.users.eq(u)
                                .and(p.id.eq(
                                        JPAExpressions
                                                .select(pSub.id.max())
                                                .from(pSub)
                                                .where(pSub.users.eq(u))
                                ))
                )
                .where(condition)
                .orderBy(orderSpecifiers.toArray(new OrderSpecifier[0]))
                .offset(offset)
                .limit(limit)
                .fetch();

        long totalCount = Optional.ofNullable(
                queryFactory
                        .select(u.id.countDistinct())
                        .from(u)
                        .where(condition)
                        .fetchOne()
        ).orElse(0L);

        return new SearchResult<>(results, totalCount);
    }

}
