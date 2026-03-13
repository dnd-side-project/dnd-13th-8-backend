package com.example.demo.domain.recommendation.repository;

import com.example.demo.domain.playlist.dto.common.PlaylistGenre;
import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.playlist.entity.QPlaylist;
import com.example.demo.domain.recommendation.dto.RecommendedUserResponse;
import com.example.demo.domain.recommendation.entity.QUserPlaylistHistory;
import com.example.demo.domain.song.entity.QSong;
import com.example.demo.domain.user.entity.QUsers;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;

import static com.example.demo.domain.follow.entity.QFollow.follow;

@RequiredArgsConstructor
public class UserPlaylistHistoryRepositoryCustomImpl implements UserPlaylistHistoryRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QUserPlaylistHistory history = QUserPlaylistHistory.userPlaylistHistory;
    private final QPlaylist playlist = QPlaylist.playlist;
    private final QSong song = QSong.song;

    @Override
    public List<Playlist> findByUserRecentGenre(String userId, int limit) {
        QUsers u = QUsers.users;

        PlaylistGenre topGenre = queryFactory
                .select(history.playlist.genre)
                .from(history)
                .where(history.user.id.eq(userId))
                .groupBy(history.playlist.genre)
                .orderBy(history.playlist.genre.count().desc())
                .limit(1)
                .fetchOne();

        if (topGenre == null) return List.of();

        return queryFactory
                .selectFrom(playlist)
                .join(playlist.users, u).fetchJoin()
                .where(playlist.genre.eq(topGenre)
                        .and(playlist.isPublic.isTrue()))
                .orderBy(playlist.visitCount.desc())
                .limit(limit)
                .fetch();
    }


    @Override
    public List<PlaylistGenre> findTopGenresByDate(LocalDate date) {
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

    @Override
    public List<PlaylistGenre> findMostPlayedGenresByUser(String userId) {
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

    @Override
    public List<Playlist> findWeeklyTopPlaylists(LocalDateTime since, int limit) {
        QUsers u = QUsers.users;

        return queryFactory
                .select(history.playlist)
                .from(history)
                .join(history.playlist, playlist)
                .join(playlist.users, u).fetchJoin()
                .where(
                        playlist.isPublic.isTrue(),
                        history.playedAt.goe(since)
                )
                .groupBy(playlist.id)
                .orderBy(history.count().desc(), playlist.id.desc())
                .limit(limit)
                .fetch();
    }

    @Override
    public List<Playlist> findLatestPlayedPlaylists(int limit) {
        QUsers u = QUsers.users;

        return queryFactory
                .select(history.playlist)
                .from(history)
                .join(history.playlist, playlist)
                .join(playlist.users, u).fetchJoin()
                .where(playlist.isPublic.isTrue())
                .groupBy(playlist.id)
                .orderBy(history.id.max().desc())
                .limit(limit)
                .fetch();
    }

    @Override
    public List<RecommendedUserResponse> findTopFollowedUsers(String userId, int limit) {
        QUsers u = QUsers.users;

        NumberExpression<Long> followerCount = follow.id.count();

        return queryFactory
                .select(Projections.constructor(
                        RecommendedUserResponse.class,
                        u.id,
                        u.username,
                        u.profileUrl,
                        u.shareCode
                ))
                .from(u)
                .leftJoin(follow).on(follow.followee.id.eq(u.id))
                .where(u.id.ne(userId))
                .groupBy(
                        u.id,
                        u.username,
                        u.profileUrl,
                        u.shareCode
                )
                .having(followerCount.gt(0L))
                .orderBy(
                        followerCount.desc(),
                        u.id.asc()
                )
                .limit(limit)
                .fetch();
    }

}
