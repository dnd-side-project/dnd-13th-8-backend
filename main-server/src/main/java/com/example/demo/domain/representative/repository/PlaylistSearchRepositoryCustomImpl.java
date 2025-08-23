package com.example.demo.domain.representative.repository;

import com.example.demo.domain.playlist.dto.PlaylistSortOption;
import com.example.demo.domain.playlist.dto.SongDto;
import com.example.demo.domain.playlist.dto.search.PlaylistSearchDto;
import com.example.demo.domain.playlist.dto.search.UserSearchDto;
import com.example.demo.domain.playlist.entity.QPlaylist;
import com.example.demo.domain.representative.entity.QRepresentativePlaylist;
import com.example.demo.domain.representative.entity.RepresentativePlaylist;
import com.example.demo.domain.song.entity.QSong;
import com.example.demo.domain.song.entity.Song;
import com.example.demo.domain.user.entity.QUsers;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;

import java.util.*;

@RequiredArgsConstructor
public class PlaylistSearchRepositoryCustomImpl implements PlaylistSearchRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QPlaylist playlist = QPlaylist.playlist;
    private final QSong song = QSong.song;

    @Override
    public List<PlaylistSearchDto> searchRepresentativePlaylists(String query, PlaylistSortOption sort, Pageable pageable) {
        QRepresentativePlaylist rp = QRepresentativePlaylist.representativePlaylist;
        QUsers u = QUsers.users;

        OrderSpecifier<?> order = switch (sort) {
            case POPULAR -> playlist.visitCount.desc();
            case RECENT  -> playlist.createdAt.desc();
        };

        List<RepresentativePlaylist> reps = queryFactory
                .selectFrom(rp)
                .distinct()
                .join(rp.playlist, playlist).fetchJoin()
                .join(playlist.users, u).fetchJoin()
                .where(playlist.name.containsIgnoreCase(query))
                .orderBy(order)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        List<Long> playlistIds = reps.stream()
                .map(rep -> rep.getPlaylist().getId())
                .toList();

        Map<Long, List<SongDto>> songMap = findSongsGroupedByPlaylistIds(playlistIds);

        return reps.stream()
                .map(rep -> {
                    var p = rep.getPlaylist();
                    var userss = p.getUsers();
                    return new PlaylistSearchDto(
                            p.getId(),
                            p.getName(),
                            userss.getId(),
                            userss.getUsername(),
                            songMap.getOrDefault(p.getId(), List.of())
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

        List<Long> playlistIds = reps.stream()
                .map(r -> r.getPlaylist().getId())
                .toList();

        Map<Long, List<SongDto>> songMap = findSongsGroupedByPlaylistIds(playlistIds);

        return reps.stream()
                .map(r -> {
                    var p = r.getPlaylist();
                    var u = r.getUser();
                    return new UserSearchDto(
                            u.getId(),
                            u.getUsername(),
                            p.getId(),
                            p.getName(),
                            songMap.getOrDefault(p.getId(), List.of())
                    );
                })
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
