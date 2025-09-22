package com.example.demo.domain.playlist.repository;

import com.example.demo.domain.follow.entity.QFollow;
import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.playlist.entity.QPlaylist;
import com.example.demo.domain.representative.entity.QRepresentativePlaylist;
import com.example.demo.domain.song.entity.QSong;
import com.example.demo.domain.user.entity.QUsers;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PlaylistRepositoryCustomImpl implements PlaylistRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    /**
     * 내가 팔로우한 유저들의 대표 플레이리스트 ID 목록 조회
     */
    @Override
    public List<Long> findFollowedRepresentativePlaylistIds(String currentUserId) {
        QFollow f = QFollow.follow;
        QRepresentativePlaylist rp = QRepresentativePlaylist.representativePlaylist;
        QPlaylist p = QPlaylist.playlist;
        QSong s = QSong.song;

        return queryFactory
                .select(rp.playlist.id)
                .from(rp)
                .join(rp.playlist, p)
                .join(s).on(s.playlist.id.eq(p.id))
                .where(rp.user.id.in(
                        com.querydsl.jpa.JPAExpressions
                                .select(f.followee.id)
                                .from(f)
                                .where(f.follower.id.eq(currentUserId))
                ))
                .groupBy(rp.playlist.id)
                .having(s.count().goe(3))
                .fetch();
    }

    /**
     * 내가 팔로우한 유저들의 대표 플레이리스트에 포함된 곡과 youtubeUrl 또는 title이 일치하는 곡이 포함된 다른 유저의 대표 플레이리스트 추천
     */
    @Override
    public List<Playlist> findPlaylistsBySimilarSongs(List<Long> basePlaylistIds, String excludeUserId,
                                                      List<Long> excludePlaylistIds, int limit) {
        QRepresentativePlaylist rp = new QRepresentativePlaylist("rp");
        QSong s1 = new QSong("s1");
        QSong s2 = new QSong("s2");
        QPlaylist p2 = new QPlaylist("p2");
        QUsers u = QUsers.users;

        return queryFactory
                .select(p2)
                .distinct()
                .from(s1)
                .join(s2).on(
                        s1.youtubeUrl.eq(s2.youtubeUrl)
                                .or(s1.youtubeTitle.eq(s2.youtubeTitle))
                )
                .join(s2.playlist, p2)
                .join(rp).on(rp.playlist.id.eq(p2.id)) // 대표 플레이리스트만
                .join(p2.users, u).fetchJoin()
                .join(QSong.song, QSong.song).on(QSong.song.playlist.id.eq(p2.id)) // 곡 개수 계산용
                .where(
                        s1.playlist.id.in(basePlaylistIds),
                        p2.id.notIn(excludePlaylistIds),
                        p2.users.id.ne(excludeUserId)
                )
                .groupBy(p2.id)
                .having(QSong.song.count().goe(3)) //  곡이 3개 이상
                .orderBy(p2.createdAt.desc())
                .limit(limit)
                .fetch();
    }

    /**
     * fallback용 최신 대표 플레이리스트 조회 (내가 만든 것 제외 + 이미 추천된 것 제외)
     */
    @Override
    public List<Playlist> findLatestRepresentativePlaylists(String excludeUserId, List<Long> excludePlaylistIds,
                                                            int limit) {
        QRepresentativePlaylist rp = QRepresentativePlaylist.representativePlaylist;
        QPlaylist p = QPlaylist.playlist;
        QUsers u = QUsers.users;
        QSong s = QSong.song;

        return queryFactory
                .select(p)
                .from(rp)
                .join(rp.playlist, p)
                .join(p.users, u).fetchJoin()
                .join(s).on(s.playlist.id.eq(p.id)) // 곡 join
                .where(
                        p.users.id.ne(excludeUserId),
                        p.id.notIn(excludePlaylistIds)
                )
                .groupBy(p.id) // count를 쓰기 위한 groupBy
                .having(s.count().goe(3)) //  곡 3개 이상
                .orderBy(p.createdAt.desc())
                .limit(limit)
                .fetch();
    }
}
