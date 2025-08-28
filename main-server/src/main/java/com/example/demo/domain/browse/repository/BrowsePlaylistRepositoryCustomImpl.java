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

    /**
     * 커서 기반 셔플 카드 조회 (내 카드만, 중복 허용)
     */
    @Override
    public List<BrowsePlaylistCard> findByUserIdWithCursorPaging(
            String userId,
            Integer cursorPosition,
            Long cursorCardId,
            int size
    ) {
        BooleanBuilder where = new BooleanBuilder()
                .and(card.userId.eq(userId));

        if (cursorPosition != null && cursorCardId != null) {
            where.and(
                    card.position.gt(cursorPosition)
                            .or(
                                    card.position.eq(cursorPosition)
                                            .and(card.id.gt(cursorCardId))
                            )
            );
        }

        return queryFactory
                .selectFrom(card)
                .where(where)
                .orderBy(card.position.asc(), card.id.asc())
                .limit(size)
                .fetch();
    }

    /**
     *  fallback용: position 기준, 다른 유저 카드 중 playlistId 중복 제거해서 하나씩만 가져오기
     */
    @Override
    public List<BrowsePlaylistCard> findDistinctByPlaylistIdWithinPosition(
            int position,
            String userId,
            int size
    ) {
        return queryFactory
                .selectFrom(card)
                .where(
                        card.position.eq(position),
                        card.userId.ne(userId)
                )
                .groupBy(card.playlistId)
                .orderBy(card.id.min().asc())
                .limit(size)
                .fetch();
    }

    public List<BrowsePlaylistCard> findFallbackWithinPositionAfter(
            int position,
            long afterCardId,
            String excludeUserId,
            int sizePlusOne
    ) {
        QBrowsePlaylistCard card = QBrowsePlaylistCard.browsePlaylistCard;

        return queryFactory
                .selectFrom(card)
                .where(
                        card.position.eq(position),
                        card.userId.ne(excludeUserId),
                        card.id.gt(afterCardId)
                )
                .groupBy(card.playlistId) // 같은 playlist 중복 제거
                .orderBy(card.id.asc())   // 가장 먼저 등장하는 카드 기준
                .limit(sizePlusOne)
                .fetch();
    }




}
