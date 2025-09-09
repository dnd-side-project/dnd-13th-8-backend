package com.example.demo.domain.recommendation.service;

import com.example.demo.domain.cd.service.CdService;
import com.example.demo.domain.playlist.dto.PlaylistGenre;
import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.playlist.repository.PlaylistRepository;
import com.example.demo.domain.recommendation.dto.PlaylistCardResponse;
import com.example.demo.domain.recommendation.dto.RecommendedGenreResponse;
import com.example.demo.domain.recommendation.repository.UserPlaylistHistoryRepository;
import com.example.demo.domain.representative.repository.RepresentativePlaylistRepository;
import com.example.demo.domain.song.entity.Song;
import com.example.demo.domain.song.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final PlaylistRepository playlistRepository;
    private final UserPlaylistHistoryRepository userPlaylistHistoryRepository;
    private final SongRepository songRepository;
    private final CdService cdService;
    private final RepresentativePlaylistRepository representativePlaylistRepository;

    private static final int RECOMMENDATION_LIMIT = 3;

    @Override
    public List<PlaylistCardResponse> getRecommendations(String userId) {
        List<Playlist> basePlaylists;
        List<Playlist> genreBased = userPlaylistHistoryRepository.findByUserRecentGenre(userId, 3);

        if (genreBased.isEmpty()) {
            basePlaylists = representativePlaylistRepository.findByVisitCount(6);
        } else {
            List<Playlist> visitCountTop3 = representativePlaylistRepository.findByVisitCount(3);
            basePlaylists = new ArrayList<>(genreBased);
            basePlaylists.addAll(visitCountTop3);
        }

        List<Long> playlistIds = basePlaylists.stream().map(Playlist::getId).toList();
        Map<Long, List<Song>> songMap = songRepository.findAllByPlaylistIdIn(playlistIds)
                .stream().collect(Collectors.groupingBy(s -> s.getPlaylist().getId()));

        return basePlaylists.stream()
                .map(p -> PlaylistCardResponse.from(p, songMap.getOrDefault(p.getId(), List.of()), cdService.getOnlyCdByPlaylistId(p.getId())))
                .toList();
    }

    @Override
    public List<PlaylistCardResponse> recommendFromLikedPlaylists(String myUserId) {
        List<Long> basePlaylistIds = playlistRepository.findFollowedRepresentativePlaylistIds(myUserId);
        List<Playlist> resultPlaylists;

        if (basePlaylistIds.isEmpty()) {
            resultPlaylists = playlistRepository.findLatestRepresentativePlaylists(myUserId, List.of(), RECOMMENDATION_LIMIT);
        } else {
            List<Playlist> basePlaylists = playlistRepository.findPlaylistsBySimilarSongs(
                    basePlaylistIds, myUserId, basePlaylistIds, RECOMMENDATION_LIMIT);

            int remain = RECOMMENDATION_LIMIT - basePlaylists.size();
            if (remain > 0) {
                List<Long> excludeIds = new ArrayList<>(basePlaylistIds);
                excludeIds.addAll(basePlaylists.stream().map(Playlist::getId).toList());

                List<Playlist> fallback = playlistRepository.findLatestRepresentativePlaylists(
                        myUserId, excludeIds, remain);
                basePlaylists.addAll(fallback);
            }
            resultPlaylists = basePlaylists;
        }

        List<Long> ids = resultPlaylists.stream().map(Playlist::getId).toList();
        Map<Long, List<Song>> songMap = songRepository.findAllByPlaylistIdIn(ids).stream()
                .collect(Collectors.groupingBy(song -> song.getPlaylist().getId()));

        return resultPlaylists.stream()
                .map(p -> PlaylistCardResponse.from(p, songMap.getOrDefault(p.getId(), List.of()), cdService.getOnlyCdByPlaylistId(p.getId())))
                .toList();
    }

    @Override
    public List<RecommendedGenreResponse> recommendGenres(String userId) {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        Set<PlaylistGenre> genres = new LinkedHashSet<>();

        List<Playlist> genreBased = userPlaylistHistoryRepository.findByUserRecentGenre(userId, 3);
        for (Playlist playlist : genreBased) {
            if (genres.size() >= 5) break;
            genres.add(playlist.getGenre());
        }

        List<PlaylistGenre> topGenres = userPlaylistHistoryRepository.findTopGenresByDate(yesterday);
        for (PlaylistGenre genre : topGenres) {
            if (genres.size() >= 5) break;
            genres.add(genre);
        }

        List<PlaylistGenre> userGenres = userPlaylistHistoryRepository.findMostPlayedGenresByUser(userId);
        for (PlaylistGenre genre : userGenres) {
            if (genres.size() >= 5) break;
            genres.add(genre);
        }

        for (PlaylistGenre genre : PlaylistGenre.values()) {
            if (genres.size() >= 5) break;
            genres.add(genre);
        }

        List<RecommendedGenreResponse> result = new ArrayList<>();
        for (PlaylistGenre genre : genres) {
            if (result.size() >= 5) break;
            result.add(RecommendedGenreResponse.from(genre));
        }

        return result;
    }
}
