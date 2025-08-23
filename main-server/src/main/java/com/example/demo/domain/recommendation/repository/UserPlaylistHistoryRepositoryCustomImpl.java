package com.example.demo.domain.recommendation.repository;


import com.example.demo.domain.playlist.dto.PlaylistGenre;
import com.example.demo.domain.playlist.entity.QPlaylist;
import com.example.demo.domain.playlist.recommendation.dto.QRecommendedPlaylistResponse;
import com.example.demo.domain.recommendation.dto.RecommendedPlaylistResponse;
import com.example.demo.domain.playlist.recommendation.entity.QUserPlaylistHistory;
import com.example.demo.domain.representative.entity.QRepresentativePlaylist;
import com.example.demo.domain.user.entity.QUsers;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
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
        QUserPlaylistHistory history = QUserPlaylistHistory.userPlaylistHistory;
        QRepresentativePlaylist rp = QRepresentativePlaylist.representativePlaylist;
        QPlaylist p = QPlaylist.playlist;
        QUsers u = QUsers.users;

        // 1. 가장 많이 들은 장르 찾기
        PlaylistGenre topGenre = queryFactory
                .select(history.playlist.genre)
                .from(history)
                .where(history.user.id.eq(userId))
                .groupBy(history.playlist.genre)
                .orderBy(history.playlist.genre.count().desc())
                .limit(1)
                .fetchOne();

        if (topGenre == null) return List.of();

        // 2. 해당 장르의 대표 플레이리스트 추천
        return queryFactory
                .select(new QRecommendedPlaylistResponse(
                        p.id,
                        p.name,
                        u.username,
                        p.genre,
                        p.visitCount
                ))
                .from(rp)
                .join(rp.playlist, p)
                .join(p.users, u)
                .where(p.genre.eq(topGenre))
                .orderBy(p.visitCount.desc())
                .limit(limit)
                .fetch();
    }


    /**
     * 조회수 많은 플레이리스트 추천
     */
    @Override
    public List<RecommendedPlaylistResponse> findByVisitCount(int limit) {
        QRepresentativePlaylist rp = QRepresentativePlaylist.representativePlaylist;
        QPlaylist p = QPlaylist.playlist;
        QUsers u = QUsers.users;

        return queryFactory
                .select(new QRecommendedPlaylistResponse(
                        p.id,
                        p.name,
                        u.username,
                        p.genre,
                        p.visitCount
                ))
                .from(rp)
                .join(rp.playlist, p)
                .join(p.users, u)
                .orderBy(p.visitCount.desc())
                .limit(limit)
                .fetch();
    }



    /**
     * 어제 기준 전체 유저의 재생 기록에서 인기 장르 최대 6개 반환
     */
    @Override
    public List<PlaylistGenre> findTopGenresByDate(LocalDate date) {
        QUserPlaylistHistory history = QUserPlaylistHistory.userPlaylistHistory;
        QPlaylist playlist = QPlaylist.playlist;

        return queryFactory
                .select(playlist.genre)
                .from(history)
                .join(history.playlist, playlist)
                .where(history.playedAt.between(date.atStartOfDay(), date.plusDays(1).atStartOfDay()))
                .groupBy(playlist.genre)
                .orderBy(history.count().desc())
                .limit(6)
                .fetch();
    }

    /**
     * 특정 사용자의 전체 재생 기록에서 많이 들은 장르 순으로 반환 (제한 없음)
     */
    @Override
    public List<PlaylistGenre> findMostPlayedGenresByUser(String userId) {
        QUserPlaylistHistory history = QUserPlaylistHistory.userPlaylistHistory;
        QPlaylist playlist = QPlaylist.playlist;
        QUsers users = QUsers.users;

        return queryFactory
                .select(playlist.genre)
                .from(history)
                .join(history.playlist, playlist)
                .join(history.user, users)
                .where(users.id.eq(userId))
                .groupBy(playlist.genre)
                .orderBy(history.count().desc())
                .fetch();
    }
}

