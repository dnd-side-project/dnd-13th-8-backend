package com.example.demo.domain.playlist.repository;

import com.example.demo.domain.like.entity.QLikes;
import com.example.demo.domain.playlist.dto.LikedPlaylistDto;
import com.example.demo.domain.playlist.dto.PlaylistDetailResponse;
import com.example.demo.domain.playlist.dto.PlaylistSearchResponse;
import com.example.demo.domain.playlist.dto.PlaylistSortOption;
import com.example.demo.domain.playlist.dto.SongDto;
import com.example.demo.domain.playlist.dto.search.PlaylistSearchDto;
import com.example.demo.domain.playlist.dto.search.UserSearchDto;
import com.example.demo.domain.playlist.entity.QPlaylist;
import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.song.entity.QSong;
import com.example.demo.domain.song.entity.Song;
import com.example.demo.domain.user.entity.QUsers;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;

import java.util.List;
import org.springframework.data.domain.Pageable;


@RequiredArgsConstructor
public class PlaylistRecommendationRepositoryCustomImpl implements PlaylistRecommendationRepositoryCustom {

    private final JPAQueryFactory queryFactory;


    public List<Playlist> findRecommendedPlaylistsByUser(String userId, int limit) {
        QPlaylist p = QPlaylist.playlist;
        QLikes l = QLikes.likes;

        // 1. 내가 좋아요 누른 플레이리스트
        List<Tuple> likedTuples = queryFactory
                .select(p.id, p.users.id)
                .from(l)
                .join(l.playlist, p)
                .where(l.users.id.eq(userId))
                .fetch();

        Set<Long> likedPlaylistIds = new HashSet<>();
        Set<String> ownerIds = new HashSet<>();

        for (Tuple tuple : likedTuples) {
            likedPlaylistIds.add(tuple.get(p.id));
            String ownerId = tuple.get(p.users.id);
            if (!ownerId.equals(userId)) {
                ownerIds.add(ownerId);
            }
        }

        List<Playlist> playlists = queryFactory
                .selectFrom(p)
                .where(
                        p.users.id.in(ownerIds),
                        p.id.notIn(likedPlaylistIds)
                )
                .orderBy(p.createdAt.desc())
                .limit(limit)
                .fetch();

        int remain = limit - playlists.size();
        if (remain > 0) {
            List<Playlist> fallback = queryFactory
                    .selectFrom(p)
                    .where(
                            p.users.id.notIn(ownerIds).and(p.users.id.ne(userId)),
                            p.id.notIn(likedPlaylistIds)
                    )
                    .orderBy(p.createdAt.desc())
                    .limit(remain)
                    .fetch();
            playlists.addAll(fallback);
        }

        return playlists;
    }
    @Override
    public List<LikedPlaylistDto> findLikedPlaylistsWithMeta(String userId, PlaylistSortOption sort, int limit) {
        QLikes pl = QLikes.likes;
        QPlaylist p = QPlaylist.playlist;
        QUsers u = QUsers.users;

        NumberExpression<Long> likeCountExpr = pl.count();

        OrderSpecifier<?> order = switch (sort) {
            case POPULAR -> p.visitCount.desc();
            case RECENT -> p.id.desc();
        };

        return queryFactory
                .select(Projections.constructor(
                        LikedPlaylistDto.class,
                        likeCountExpr,
                        u.username,
                        u.id
                ))
                .from(pl)
                .join(pl.playlist, p)
                .join(p.users, u)
                .where(pl.users.id.eq(userId))
                .groupBy(p.id, u.username,  u.id)
                .orderBy(order)
                .limit(limit)
                .fetch();
    }

    @Override
    public List<PlaylistDetailResponse> findPlaylistsWithSongsByCreatorId(String creatorId) {
        QPlaylist p = QPlaylist.playlist;
        QUsers u = QUsers.users;
        QSong s = QSong.song;

        // 1. 플레이리스트 + 곡까지 모두 가져오기
        List<Tuple> tuples = queryFactory
                .select(p, s)
                .from(p)
                .join(p.users, u)
                .leftJoin(s).on(s.playlist.eq(p))
                .where(u.id.eq(creatorId))
                .fetch();

        // 2. 플레이리스트 ID 기준으로 곡들을 묶기
        Map<Long, PlaylistDetailResponse> playlistMap = new LinkedHashMap<>();

        for (Tuple tuple : tuples) {
            Playlist playlist = tuple.get(p);
            Song song = tuple.get(s);

            PlaylistDetailResponse dto = playlistMap.get(playlist.getId());

            if (dto == null) {
                dto = new PlaylistDetailResponse(
                        playlist.getId(),
                        playlist.getName(),
                        playlist.getIsRepresentative(),
                        new ArrayList<>(),
                        playlist.getGenre()

                );
                playlistMap.put(playlist.getId(), dto);
            }

            if (song != null) {
                dto.tracks().add(SongDto.from(song));
            }
        }

        return new ArrayList<>(playlistMap.values());
    }

    @Override
    public List<PlaylistSearchDto> searchPlaylists(String query, PlaylistSortOption sort, Pageable pageable) {
        QPlaylist p = QPlaylist.playlist;
        QUsers u = QUsers.users;

        OrderSpecifier<?> order = switch (sort) {
            case POPULAR -> p.visitCount.desc();
            case RECENT -> p.createdAt.desc();
        };

        return queryFactory
                .select(Projections.constructor(
                        PlaylistSearchDto.class,
                        p.id,
                        p.name
                ))
                .from(p)
                .join(p.users, u)
                .where(p.name.containsIgnoreCase(query))
                .orderBy(order)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    @Override
    public List<UserSearchDto> searchUsersWithRepresentativePlaylist(String query) {
        QPlaylist p = QPlaylist.playlist;
        QUsers u = QUsers.users;

        return queryFactory
                .select(Projections.constructor(
                        UserSearchDto.class,
                        u.id,
                        u.username
                ))
                .from(p)
                .join(p.users, u)
                .where(
                        u.username.containsIgnoreCase(query)
                                .and(p.isRepresentative.isTrue())
                )
                .fetch();
    }
}
