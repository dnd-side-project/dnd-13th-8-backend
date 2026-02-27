package com.example.demo.domain.user.repository;

import com.example.demo.domain.playlist.dto.common.PlaylistSortOption;
import com.example.demo.domain.playlist.dto.search.SearchResult;
import com.example.demo.domain.playlist.dto.search.SearchType;
import com.example.demo.domain.playlist.dto.search.UserSearchDto;
import com.example.demo.domain.user.entity.QUsers;
import com.example.demo.global.jwt.JwtRoleType;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
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

        BooleanBuilder condition = new BooleanBuilder()
                .and(u.username.containsIgnoreCase(query))
                .and(u.role.in(JwtRoleType.SUPER, JwtRoleType.USER));

        List<UserSearchDto> results = queryFactory
                .select(Projections.constructor(
                        UserSearchDto.class,
                        Expressions.constant(SearchType.USER),
                        u.id,
                        u.shareCode,
                        u.username,
                        u.profileUrl
                ))
                .from(u)
                .where(condition)
                .orderBy(u.id.desc())
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
