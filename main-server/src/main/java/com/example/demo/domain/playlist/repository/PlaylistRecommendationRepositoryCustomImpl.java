package com.example.demo.domain.playlist.repository;

import com.example.demo.domain.like.entity.QLikes;
import com.example.demo.domain.playlist.entity.QPlaylist;
import com.example.demo.domain.playlist.entity.Playlist;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;

import java.util.List;


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
}
