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

        // 내가 팔로우한 유저 ID 조회 (playlist 기준 아님)
        List<String> followedUserIds = queryFactory
                .select(f.playlist.users.id)
                .from(f)
                .where(f.users.id.eq(currentUserId))
                .distinct()
                .fetch();

        if (followedUserIds.isEmpty()) {
            return List.of();
        }

        // 팔로우한 유저들의 대표 플레이리스트 ID 조회
        return queryFactory
                .select(rp.playlist.id)
                .from(rp)
                .where(rp.user.id.in(followedUserIds))
                .fetch();
    }

    /**
     * 내가 팔로우한 유저들의 대표 플레이리스트에 포함된 곡과
     * youtubeUrl 또는 title이 일치하는 곡이 포함된
     * 다른 유저의 대표 플레이리스트 추천
     */
    @Override
    public List<Playlist> findPlaylistsBySimilarSongs(List<Long> basePlaylistIds, String excludeUserId, List<Long> excludePlaylistIds, int limit) {
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
                .join(rp).on(rp.playlist.id.eq(p2.id)) // 대표 플레이리스트인지 확인
                .join(p2.users, u).fetchJoin()
                .where(
                        s1.playlist.id.in(basePlaylistIds),   // 기준 곡: 팔로우한 유저의 대표 플리 곡
                        p2.id.notIn(excludePlaylistIds),      // 이미 추천된 플리 제외
                        p2.users.id.ne(excludeUserId)         // 본인 제외
                )
                .orderBy(p2.createdAt.desc())
                .limit(limit)
                .fetch();
    }

    /**
     * fallback용 최신 대표 플레이리스트 조회
     * (내가 만든 것 제외 + 이미 추천된 것 제외)
     */
    @Override
    public List<Playlist> findLatestRepresentativePlaylists(String excludeUserId, List<Long> excludePlaylistIds, int limit) {
        QRepresentativePlaylist rp = QRepresentativePlaylist.representativePlaylist;
        QPlaylist p = QPlaylist.playlist;
        QUsers u = QUsers.users;

        return queryFactory
                .select(p)
                .from(rp)
                .join(rp.playlist, p) // ️ fetchJoin 제거
                .join(p.users, u).fetchJoin()
                .where(
                        p.users.id.ne(excludeUserId),
                        p.id.notIn(excludePlaylistIds)
                )
                .orderBy(p.createdAt.desc())
                .limit(limit)
                .fetch();
    }

}
