package com.example.demo.domain.representative.repository;

import com.example.demo.domain.playlist.dto.PlaylistSortOption;
import com.example.demo.domain.playlist.dto.SongDto;
import com.example.demo.domain.playlist.dto.search.PlaylistSearchDto;
import com.example.demo.domain.playlist.dto.search.UserSearchDto;
import com.example.demo.domain.playlist.entity.QPlaylist;
import com.example.demo.domain.representative.entity.QRepresentativePlaylist;
import com.example.demo.domain.representative.entity.RepresentativePlaylist;
import com.example.demo.domain.user.entity.QUsers;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RequiredArgsConstructor
public class PlaylistSearchRepositoryCustomImpl implements PlaylistSearchRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<PlaylistSearchDto> searchRepresentativePlaylists(String query, PlaylistSortOption sort, Pageable pageable) {
        QRepresentativePlaylist rp = QRepresentativePlaylist.representativePlaylist;
        QPlaylist p = QPlaylist.playlist;
        QUsers u = QUsers.users;

        OrderSpecifier<?> order = switch (sort) {
            case POPULAR -> p.visitCount.desc();
            case RECENT  -> p.createdAt.desc();
        };

        List<RepresentativePlaylist> reps = queryFactory
                .selectFrom(rp)           // 엔티티 선택
                .distinct()               // 중복 방지
                .join(rp.playlist, p).fetchJoin()
                .join(p.users, u).fetchJoin()
                .where(p.name.containsIgnoreCase(query))
                .orderBy(order)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return reps.stream()
                .map(rep -> {
                    var playlist = rep.getPlaylist();
                    var user = playlist.getUsers();
                    var songs = playlist.getSongs().stream()
                            .map(SongDto::from)
                            .toList();

                    return new PlaylistSearchDto(
                            playlist.getId(),
                            playlist.getName(),
                            user.getId(),
                            user.getUsername(),
                            songs
                    );
                })
                .toList();

}

    @Override
    public List<UserSearchDto> searchUsersWithRepresentativePlaylist(String query, Pageable pageable) {
        QRepresentativePlaylist rep = QRepresentativePlaylist.representativePlaylist;
        QPlaylist playlist = QPlaylist.playlist;
        QUsers user = QUsers.users;

        List<RepresentativePlaylist> reps = queryFactory
                .selectFrom(rep)
                .join(rep.playlist, playlist).fetchJoin()
                .join(rep.user, user).fetchJoin()
                .where(user.username.containsIgnoreCase(query))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return reps.stream()
                .map(r -> {
                    var p = r.getPlaylist();
                    var u = r.getUser();
                    var songs = p.getSongs().stream().map(SongDto::from).toList();
                    return new UserSearchDto(
                            u.getId(),
                            u.getUsername(),
                            p.getId(),
                            p.getName(),
                            songs
                    );
                })
                .toList();

}
}

