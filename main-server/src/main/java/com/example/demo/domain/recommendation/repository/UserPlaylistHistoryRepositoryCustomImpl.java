package com.example.demo.domain.recommendation.repository;

import com.example.demo.domain.playlist.entity.QPlaylist;
import com.example.demo.domain.recommendation.dto.QRecommendedPlaylistResponse;
import com.example.demo.domain.recommendation.dto.RecommendedPlaylistResponse;
import com.example.demo.domain.recommendation.entity.QUserPlaylistHistory;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;


@RequiredArgsConstructor
public class UserPlaylistHistoryRepositoryCustomImpl implements UserPlaylistHistoryRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private final QUserPlaylistHistory history = QUserPlaylistHistory.userPlaylistHistory;
    private final QPlaylist playlist = QPlaylist.playlist;

    /**
     * 사용자가 최근 가장 많이 들은 장르 기반 추천
     */
    @Override
    public List<RecommendedPlaylistResponse> findByUserRecentGenre(String userId, int limit) {
        // 사용자가 가장 많이 들은 장르 하나 찾기
        var topGenre = queryFactory
                .select(history.playlist.genre)
                .from(history)
                .where(history.user.id.eq(userId))
                .groupBy(history.playlist.genre)
                .orderBy(history.playlist.genre.count().desc())
                .limit(1)
                .fetchOne();

        if (topGenre == null) {
            return List.of();
        }

        // 해당 장르 기반 추천 플레이리스트
        return queryFactory
                .select(new QRecommendedPlaylistResponse(
                        playlist.id,
                        playlist.name,
                        playlist.users.username,
                        playlist.genre,
                        playlist.visitCount
                ))
                .from(playlist)
                .where(playlist.genre.eq(topGenre))
                .limit(limit)
                .fetch();
    }

    /**
     * 좋아요 수가 많은 플레이리스트 추천
     */
    @Override
    public List<RecommendedPlaylistResponse> findByLikeCount(int limit) {
        return queryFactory
                .select(new QRecommendedPlaylistResponse(
                        playlist.id,
                        playlist.name,
                        playlist.users.username,
                        playlist.genre,
                        playlist.visitCount
                ))
                .from(playlist)
                .orderBy(playlist.visitCount.desc())
                .limit(limit)
                .fetch();
    }
}

