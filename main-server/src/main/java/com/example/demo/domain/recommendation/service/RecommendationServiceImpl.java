package com.example.demo.domain.recommendation.service;

import com.example.demo.domain.cd.dto.response.CdItemsByPlaylist;
import com.example.demo.domain.cd.service.CdService;
import com.example.demo.domain.playlist.dto.common.PlaylistGenre;
import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.playlist.repository.PlaylistRepository;
import com.example.demo.domain.recommendation.dto.GetTimeRecommendationResponse;
import com.example.demo.domain.recommendation.dto.RecommendedPlaylistResponse;
import com.example.demo.domain.recommendation.dto.RecommendedGenreResponse;
import com.example.demo.domain.recommendation.dto.RecommendedUserResponse;
import com.example.demo.domain.recommendation.entity.bundle.Bundle;
import com.example.demo.domain.recommendation.entity.bundle.BundlePlaylist;
import com.example.demo.domain.recommendation.entity.bundle.BundleTimeSlot;
import com.example.demo.domain.recommendation.repository.UserPlaylistHistoryRepository;
import com.example.demo.domain.recommendation.repository.bundle.BundlePlaylistRepository;
import com.example.demo.domain.recommendation.repository.bundle.BundleRepository;
import com.example.demo.domain.song.dto.SongsByPlaylist;
import com.example.demo.domain.song.service.SongService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final PlaylistRepository playlistRepository;
    private final UserPlaylistHistoryRepository userPlaylistHistoryRepository;
    private final BundlePlaylistRepository bundlePlaylistRepository;
    private final BundleRepository bundleRepository;
    private final SongService songService;
    private final CdService cdService;

    private static final int RECOMMENDATION_LIMIT = 3;

    @Override
    @Transactional
    public List<RecommendedPlaylistResponse> getRecommendations(String userId) {
        List<Playlist> genreBased = userPlaylistHistoryRepository.findByUserRecentGenre(userId, 3);
        List<Playlist> visitCountTop6 = playlistRepository.findByVisitCount(6);

        Set<Long> genreIds = genreBased.stream()
                .map(Playlist::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<Playlist> basePlaylists = Stream.concat(
                genreBased.stream(),
                visitCountTop6.stream().filter(p -> !genreIds.contains(p.getId()))
        ).limit(6).toList();

        List<Long> playlistIds = basePlaylists.stream().map(Playlist::getId).toList();

        SongsByPlaylist songsByPlaylist = songService.findSongsByPlaylistIdsIn(playlistIds);

        CdItemsByPlaylist cdItemsByPlaylist = cdService.findCdItemsByPlaylistIdsIn(playlistIds);

        return basePlaylists.stream()
                .map(p -> RecommendedPlaylistResponse.from(
                        p,
                        songsByPlaylist.songsOf(p.getId()),
                        cdItemsByPlaylist.cdItemsOf(p.getId())
                ))
                .toList();
    }

    @Override
    @Transactional
    public List<RecommendedPlaylistResponse> recommendFromLikedPlaylists(String myUserId) {
        List<Long> basePlaylistIds = playlistRepository.findFollowedPlaylistIds(myUserId);
        List<Playlist> resultPlaylists;

        if (basePlaylistIds.isEmpty()) {
            resultPlaylists = playlistRepository.findLatestPlaylists(myUserId, List.of(), RECOMMENDATION_LIMIT);
        } else {
            List<Playlist> basePlaylists = playlistRepository.findPlaylistsBySimilarSongs(
                    basePlaylistIds, myUserId, basePlaylistIds, RECOMMENDATION_LIMIT);

            int remain = RECOMMENDATION_LIMIT - basePlaylists.size();
            if (remain > 0) {
                List<Long> excludeIds = new ArrayList<>(basePlaylistIds);
                excludeIds.addAll(basePlaylists.stream().map(Playlist::getId).toList());

                List<Playlist> fallback = playlistRepository.findLatestPlaylists(
                        myUserId, excludeIds, remain);
                basePlaylists.addAll(fallback);
            }
            resultPlaylists = basePlaylists;
        }

        List<Long> ids = resultPlaylists.stream().map(Playlist::getId).toList();
        SongsByPlaylist songsByPlaylist = songService.findSongsByPlaylistIdsIn(ids);
        CdItemsByPlaylist cdItemsByPlaylist = cdService.findCdItemsByPlaylistIdsIn(ids);

        return resultPlaylists.stream()
                .map(p -> RecommendedPlaylistResponse.from(
                        p,
                        songsByPlaylist.songsOf(p.getId()),
                        cdItemsByPlaylist.cdItemsOf(p.getId())
                ))
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

    @Override
    @Transactional(readOnly = true)
    public List<RecommendedPlaylistResponse> getAdminRecommendation(int limit) {

        List<Playlist> adminPlaylists = playlistRepository.findAdminPlaylists(limit);

        if (adminPlaylists.isEmpty()) {
            return List.of();
        }

        List<Long> playlistIds = adminPlaylists.stream()
                .map(Playlist::getId)
                .toList();

        SongsByPlaylist songsByPlaylist = songService.findSongsByPlaylistIdsIn(playlistIds);
        CdItemsByPlaylist cdItemsByPlaylist = cdService.findCdItemsByPlaylistIdsIn(playlistIds);

        return adminPlaylists.stream()
                .map(p -> RecommendedPlaylistResponse.from(
                        p,
                        songsByPlaylist.songsOf(p.getId()),
                        cdItemsByPlaylist.cdItemsOf(p.getId())
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecommendedPlaylistResponse> getWeeklyTopRecommendation(int limit) {

        LocalDateTime now = LocalDateTime.now();

        LinkedHashMap<Long, Playlist> result = new LinkedHashMap<>();

        userPlaylistHistoryRepository
                .findWeeklyTopPlaylists(now.minusDays(7), limit)
                .forEach(p -> result.putIfAbsent(p.getId(), p));

        if (result.size() < limit) {
            int remain = limit - result.size();

            userPlaylistHistoryRepository
                    .findLatestPlayedPlaylists(remain)
                    .forEach(p -> result.putIfAbsent(p.getId(), p));
        }

        List<Playlist> playlists = result.values()
                .stream()
                .limit(limit)
                .toList();

        if (playlists.isEmpty()) return List.of();

        List<Long> ids = playlists.stream()
                .map(Playlist::getId)
                .toList();

        SongsByPlaylist songsByPlaylist = songService.findSongsByPlaylistIdsIn(ids);
        CdItemsByPlaylist cdItemsByPlaylist = cdService.findCdItemsByPlaylistIdsIn(ids);

        return playlists.stream()
                .map(p -> RecommendedPlaylistResponse.from(
                        p,
                        songsByPlaylist.songsOf(p.getId()),
                        cdItemsByPlaylist.cdItemsOf(p.getId())
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecommendedUserResponse> recommendTopFollowedUsers(String userId, int limit) {
        return userPlaylistHistoryRepository.findTopFollowedUsers(userId, limit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GetTimeRecommendationResponse> getTimeRecommendation(BundleTimeSlot timeSlot, String userId) {

        List<Bundle> bundles = bundleRepository.findByTimeSlotOrderByIdAsc(timeSlot);

        if (bundles.isEmpty()) return List.of();

        List<Long> bundleIds = bundles.stream()
                .map(Bundle::getId)
                .toList();

        List<BundlePlaylist> bundlePlaylists =
                bundlePlaylistRepository.findByBundleIdsWithPlaylistAndUser(bundleIds);

        List<Playlist> playlists = bundlePlaylists.stream()
                .map(BundlePlaylist::getPlaylist)
                .toList();

        List<Long> playlistIds = playlists.stream()
                .map(Playlist::getId)
                .distinct()
                .toList();

        SongsByPlaylist songsByPlaylist =
                songService.findSongsByPlaylistIdsIn(playlistIds);

        CdItemsByPlaylist cdItemsByPlaylist =
                cdService.findCdItemsByPlaylistIdsIn(playlistIds);

        Map<Long, List<RecommendedPlaylistResponse>> playlistMap = new LinkedHashMap<>();

        for (BundlePlaylist bp : bundlePlaylists) {

            Playlist playlist = bp.getPlaylist();
            Long bundleId = bp.getBundle().getId();

            RecommendedPlaylistResponse response =
                    RecommendedPlaylistResponse.from(
                            playlist,
                            songsByPlaylist.songsOf(playlist.getId()),
                            cdItemsByPlaylist.cdItemsOf(playlist.getId())
                    );

            playlistMap.computeIfAbsent(bundleId, k -> new ArrayList<>())
                    .add(response);
        }

        return bundles.stream()
                .map(bundle -> new GetTimeRecommendationResponse(
                        bundle.getId(),
                        bundle.getTitle(),
                        bundle.getTimeSlot(),
                        playlistMap.getOrDefault(bundle.getId(), List.of())
                ))
                .toList();
    }
}
