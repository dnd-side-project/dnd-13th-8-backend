package com.example.demo.domain.playlist.repository.query;

import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.playlist.entity.QPlaylist;
import com.example.demo.domain.user.entity.QUsers;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PlaylistRecommendationQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<Playlist> findByVisitCount(int limit) {
        QPlaylist p = QPlaylist.playlist;
        QUsers u = QUsers.users;

        return queryFactory
                .selectFrom(p)
                .join(p.users, u).fetchJoin()
                .where(p.isPublic.isTrue())
                .orderBy(p.visitCount.desc(), p.id.desc())
                .limit(limit)
                .fetch();
    }

    public List<Playlist> findAdminPlaylists(int limit) {
        QPlaylist p = QPlaylist.playlist;
        QUsers u = QUsers.users;

        return queryFactory
                .selectFrom(p)
                .join(p.users, u).fetchJoin()
                .where(
                        u.shareCode.eq("admin"),
                        p.isPublic.isTrue()
                )
                .orderBy(p.id.desc())
                .limit(limit)
                .fetch();
    }
}
