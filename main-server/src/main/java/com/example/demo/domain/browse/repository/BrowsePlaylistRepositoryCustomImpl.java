package com.example.demo.domain.browse.repository;

import com.example.demo.domain.browse.entity.BrowsePlaylistCard;
import com.example.demo.domain.browse.entity.QBrowsePlaylistCard;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BrowsePlaylistRepositoryCustomImpl implements BrowsePlaylistRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QBrowsePlaylistCard card = QBrowsePlaylistCard.browsePlaylistCard;

    @Override
    public List<BrowsePlaylistCard> findByUserIdWithCursorPaging(String userId, Integer cursorPosition,
                                                                 Long cursorCardId, int size) {
            // 기본 조건: 해당 유저의 카드만 조회
            BooleanBuilder where = new BooleanBuilder()
                    .and(card.userId.eq(userId));

            // 커서가 존재할 경우 → position + cardId 조합으로 이후 데이터 조회
            if (cursorPosition != null && cursorCardId != null) {
                where.and(
                        card.position.gt(cursorPosition)
                                .or(
                                        card.position.eq(cursorPosition)
                                                .and(card.id.gt(cursorCardId))
                                )
                );
            }

            // 정렬: position → cardId 순으로 정렬하여 커서 기준 일관성 유지
            return queryFactory
                    .selectFrom(card)
                    .where(where)
                    .orderBy(card.position.asc(), card.id.asc())
                    .limit(size) // 페이지 크기 제한
                    .fetch();
        }
}
