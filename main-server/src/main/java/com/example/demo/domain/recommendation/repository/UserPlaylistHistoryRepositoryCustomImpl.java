package com.example.demo.domain.recommendation.repository;

import com.example.demo.domain.representative.entity.RepresentativePlaylist;
import com.example.demo.domain.follow.entity.QFollow;
import com.example.demo.domain.playlist.dto.PlaylistGenre;
import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.playlist.entity.QPlaylist;
import com.example.demo.domain.recommendation.entity.QUserPlaylistHistory;
import com.example.demo.domain.representative.entity.QRepresentativePlaylist;
import com.example.demo.domain.song.entity.QSong;
import com.example.demo.domain.user.entity.QUsers;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserPlaylistHistoryRepositoryCustomImpl implements UserPlaylistHistoryRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QUserPlaylistHistory history = QUserPlaylistHistory.userPlaylistHistory;
    private final QPlaylist playlist = QPlaylist.playlist;
    private final QSong song = QSong.song;

    @Override
    public List<Playlist> findByUserRecentGenre(String userId, int limit) {
        QRepresentativePlaylist rp = QRepresentativePlaylist.representativePlaylist;
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
                .select(rp) //  select owner entity
                .from(rp)
                .join(rp.playlist, playlist).fetchJoin()
                .join(playlist.users, u).fetchJoin()
                .where(playlist.genre.eq(topGenre))
                .orderBy(playlist.visitCount.desc())
                .limit(limit)
                .fetch()
                .stream()
                .map(RepresentativePlaylist::getPlaylist)
                .toList();
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

}
