package com.example.demo.domain.recommendation.repository;

import com.example.demo.domain.follow.entity.QFollow;
import com.example.demo.domain.playlist.dto.PlaylistGenre;
import com.example.demo.domain.playlist.dto.SongDto;
import com.example.demo.domain.playlist.dto.search.PlaylistSearchDto;
import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.playlist.entity.QPlaylist;
import com.example.demo.domain.recommendation.entity.QUserPlaylistHistory;
import com.example.demo.domain.representative.entity.QRepresentativePlaylist;
import com.example.demo.domain.song.entity.QSong;
import com.example.demo.domain.song.entity.Song;
import com.example.demo.domain.user.entity.QUsers;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.*;

@RequiredArgsConstructor
public class UserPlaylistHistoryRepositoryCustomImpl implements UserPlaylistHistoryRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QUserPlaylistHistory history = QUserPlaylistHistory.userPlaylistHistory;
    private final QPlaylist playlist = QPlaylist.playlist;
    private final QSong song = QSong.song;

    @Override
    public List<PlaylistSearchDto> findByUserRecentGenre(String userId, int limit) {
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

        List<Playlist> playlists = queryFactory
                .select(rp.playlist)
                .from(rp)
                .join(rp.playlist, playlist).fetchJoin()
                .join(playlist.users, u).fetchJoin()
                .where(playlist.genre.eq(topGenre))
                .orderBy(playlist.visitCount.desc())
                .limit(limit)
                .fetch();

        return toDtoWithSongs(playlists);
    }

    @Override
    public List<PlaylistSearchDto> findByVisitCount(int limit) {
        QRepresentativePlaylist rp = QRepresentativePlaylist.representativePlaylist;
        QUsers u = QUsers.users;

        List<Playlist> playlists = queryFactory
                .select(rp.playlist)
                .from(rp)
                .join(rp.playlist, playlist).fetchJoin()
                .join(playlist.users, u).fetchJoin()
                .orderBy(playlist.visitCount.desc())
                .limit(limit)
                .fetch();

        return toDtoWithSongs(playlists);
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
    public List<PlaylistSearchDto> findRecommendedPlaylistsByUser(String userId, int limit) {
        QFollow f = QFollow.follow;
        QUsers u = QUsers.users;

        List<Long> followedPlaylistIds = queryFactory
                .select(f.playlist.id)
                .from(f)
                .where(f.users.id.eq(userId))
                .fetch();

        List<Playlist> basePlaylists = queryFactory
                .selectFrom(playlist)
                .join(playlist.users, u).fetchJoin()
                .where(
                        playlist.id.notIn(followedPlaylistIds),
                        playlist.users.id.ne(userId)
                )
                .orderBy(playlist.createdAt.desc())
                .limit(limit)
                .fetch();

        int remain = limit - basePlaylists.size();
        if (remain > 0) {
            List<Playlist> fallback = queryFactory
                    .selectFrom(playlist)
                    .join(playlist.users, u).fetchJoin()
                    .where(
                            playlist.id.notIn(followedPlaylistIds),
                            playlist.users.id.ne(userId),
                            playlist.id.notIn(basePlaylists.stream().map(Playlist::getId).toList())
                    )
                    .orderBy(playlist.createdAt.desc())
                    .limit(remain)
                    .fetch();

            basePlaylists.addAll(fallback);
        }

        return toDtoWithSongs(basePlaylists);
    }

    private List<PlaylistSearchDto> toDtoWithSongs(List<Playlist> playlists) {
        List<Long> playlistIds = playlists.stream().map(Playlist::getId).toList();
        Map<Long, List<SongDto>> songMap = findSongsGroupedByPlaylistIds(playlistIds);

        return playlists.stream()
                .map(p -> new PlaylistSearchDto(
                        p.getId(),
                        p.getName(),
                        p.getUsers().getId(),
                        p.getUsers().getUsername(),
                        songMap.getOrDefault(p.getId(), List.of())
                ))
                .toList();
    }

    private Map<Long, List<SongDto>> findSongsGroupedByPlaylistIds(List<Long> playlistIds) {
        List<Tuple> rows = queryFactory
                .select(song, song.playlist.id)
                .from(song)
                .where(song.playlist.id.in(playlistIds))
                .fetch();

        Map<Long, List<SongDto>> result = new HashMap<>();
        for (Tuple row : rows) {
            Song s = row.get(song);
            Long playlistId = row.get(song.playlist.id);

            result.computeIfAbsent(playlistId, k -> new ArrayList<>())
                    .add(SongDto.from(s));
        }
        return result;
    }
}
