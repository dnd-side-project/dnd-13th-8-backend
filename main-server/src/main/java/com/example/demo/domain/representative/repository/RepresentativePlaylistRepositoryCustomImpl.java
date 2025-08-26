package com.example.demo.domain.representative.repository;

import com.example.demo.domain.playlist.dto.PlaylistGenre;
import com.example.demo.domain.playlist.dto.PlaylistSortOption;
import com.example.demo.domain.playlist.dto.SongDto;
import com.example.demo.domain.playlist.dto.search.PlaylistSearchDto;
import com.example.demo.domain.playlist.dto.search.UserSearchDto;
import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.playlist.entity.QPlaylist;
import com.example.demo.domain.representative.entity.QRepresentativePlaylist;
import com.example.demo.domain.representative.entity.RepresentativePlaylist;
import com.example.demo.domain.song.entity.QSong;
import com.example.demo.domain.song.entity.Song;
import com.example.demo.domain.user.entity.QUsers;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;

import java.util.*;

@RequiredArgsConstructor
public class RepresentativePlaylistRepositoryCustomImpl implements RepresentativePlaylistRepositoryCustom {

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

    @Override
    public List<Playlist> findByVisitCount(int limit) {
        QRepresentativePlaylist rp = QRepresentativePlaylist.representativePlaylist;
        QUsers u = QUsers.users;

        return queryFactory
                .select(rp)
                .from(rp)
                .join(rp.playlist, playlist).fetchJoin()
                .join(playlist.users, u).fetchJoin()
                .orderBy(playlist.visitCount.desc())
                .limit(limit)
                .fetch()
                .stream()
                .map(RepresentativePlaylist::getPlaylist)
                .toList();
    }

    @Override
    public List<Playlist> findTopVisitedRepresentativePlaylistsByGenres(Set<PlaylistGenre> genres) {
        QRepresentativePlaylist rep = QRepresentativePlaylist.representativePlaylist;
        QPlaylist playlist = QPlaylist.playlist;

        if (genres == null || genres.isEmpty()) {
            return List.of();
        }

        List<RepresentativePlaylist> reps = queryFactory
                .selectFrom(rep)
                .join(rep.playlist, playlist).fetchJoin()
                .where(playlist.genre.in(genres))
                .orderBy(playlist.genre.asc(), playlist.visitCount.desc())
                .fetch();

        // 장르별 상위 1개씩만 추출
        Map<PlaylistGenre, Playlist> topByGenre = reps.stream()
                .map(RepresentativePlaylist::getPlaylist)
                .collect(Collectors.toMap(
                        Playlist::getGenre,
                        Function.identity(),
                        (existing, replacement) -> existing // visitCount 높은 게 먼저 오므로 그대로 유지
                ));

        return new ArrayList<>(topByGenre.values());
    }

}
