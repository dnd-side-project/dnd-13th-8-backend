package com.example.demo.domain.playlist.service;


import com.example.demo.domain.playlist.dto.playlistdto.PlaylistDetailResponse;
import com.example.demo.domain.playlist.dto.PlaylistGenre;
import com.example.demo.domain.playlist.dto.SongDto;
import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.playlist.repository.PlaylistRepository;
import com.example.demo.domain.recommendation.dto.PlaylistCardResponse;
import com.example.demo.domain.recommendation.entity.UserPlaylistHistory;
import com.example.demo.domain.recommendation.repository.UserPlaylistHistoryRepository;
import com.example.demo.domain.song.entity.Song;
import com.example.demo.domain.song.repository.SongRepository;
import com.example.demo.domain.user.repository.UsersRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaylistMainPageServiceImpl implements PlaylistMainPageService {

    private final PlaylistRepository playlistRepository;
    private final UsersRepository userRepository;
    private final UserPlaylistHistoryRepository userPlaylistHistoryRepository;
    private final SongRepository songRepository;

    private static final int RECOMMENDATION_LIMIT = 3;

    @Override
    @Transactional
    public PlaylistDetailResponse getPlaylistDetail(Long playlistId, String userId) {
        var playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new IllegalArgumentException("플레이리스트를 찾을 수 없습니다."));

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<Song> songs = songRepository.findSongsByPlaylistId(playlistId);

        List<SongDto> songDtos = songs.stream()
                .map(SongDto::from)
                .toList();

        // 1) 재생 기록 추가
        userPlaylistHistoryRepository.save(UserPlaylistHistory.of(user, playlist));

        // 2) 방문 수 증가
        playlistRepository.incrementVisitCount(playlistId);

        return PlaylistDetailResponse.from(playlist, songDtos);
    }

    /*
    첫번째 추천 알고리즘
     */
    @Override
    public List<PlaylistCardResponse> getRecommendations(String userId) {
        List<Playlist> basePlaylists;

        List<Playlist> genreBased = userPlaylistHistoryRepository.findByUserRecentGenre(userId, 3);

        if (genreBased.isEmpty()) {
            basePlaylists = userPlaylistHistoryRepository.findByVisitCount(6);
        } else {
            List<Playlist> visitCountTop3 = userPlaylistHistoryRepository.findByVisitCount(3);
            basePlaylists = new ArrayList<>();
            basePlaylists.addAll(genreBased);
            basePlaylists.addAll(visitCountTop3);
        }

        List<Long> playlistIds = basePlaylists.stream()
                .map(Playlist::getId)
                .toList();

        Map<Long, List<Song>> songMap = songRepository.findAllByPlaylistIdIn(playlistIds)
                .stream()
                .collect(Collectors.groupingBy(s -> s.getPlaylist().getId()));

        return basePlaylists.stream()
                .map(p -> PlaylistCardResponse.from(p, songMap.getOrDefault(p.getId(), List.of())))
                .toList();
    }



    /*
    두번째 추천 알고리즘
     */
    @Override
    public List<PlaylistCardResponse> recommendFromLikedPlaylists(String myUserId) {
        List<Playlist> playlists = userPlaylistHistoryRepository.findRecommendedPlaylistsByUser(myUserId, RECOMMENDATION_LIMIT);

        List<Long> ids = playlists.stream().map(Playlist::getId).toList();
        Map<Long, List<Song>> songMap = songRepository.findAllByPlaylistIdIn(ids)
                .stream()
                .collect(Collectors.groupingBy(s -> s.getPlaylist().getId()));

        return playlists.stream()
                .map(p -> PlaylistCardResponse.from(p, songMap.getOrDefault(p.getId(), List.of())))
                .toList();
    }



    @Override
    public List<PlaylistDetailResponse> recommendGenres(String userId) {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        Set<PlaylistGenre> genres = new LinkedHashSet<>(userPlaylistHistoryRepository.findTopGenresByDate(yesterday));

        if (genres.size() < 6) {
            for (PlaylistGenre genre : userPlaylistHistoryRepository.findMostPlayedGenresByUser(userId)) {
                if (genres.size() >= 6) break;
                genres.add(genre);
            }
        }

        if (genres.size() < 6) {
            for (PlaylistGenre genre : PlaylistGenre.values()) {
                if (genres.size() >= 6) break;
                genres.add(genre);
            }
        }

        return genres.stream()
                .limit(6)
                .map(genre -> {
                    Playlist playlist = playlistRepository.findTopByGenreOrderByVisitCountDesc(genre)
                            .orElseThrow(() -> new IllegalStateException("No playlist found for genre: " + genre));
                    List<SongDto> songs = songRepository.findSongsByPlaylistId(playlist.getId())
                            .stream().map(SongDto::from).toList();
                    return PlaylistDetailResponse.from(playlist, songs);
                })
                .toList();
    }

}

