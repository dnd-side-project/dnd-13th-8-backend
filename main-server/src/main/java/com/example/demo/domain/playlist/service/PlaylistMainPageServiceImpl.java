package com.example.demo.domain.playlist.service;


import com.example.demo.domain.cd.service.CdService;
import com.example.demo.domain.playlist.dto.playlistdto.MainPlaylistDetailResponse;
import com.example.demo.domain.playlist.dto.PlaylistGenre;
import com.example.demo.domain.playlist.dto.SongDto;
import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.playlist.repository.PlaylistRepository;
import com.example.demo.domain.recommendation.dto.PlaylistCardResponse;
import com.example.demo.domain.recommendation.dto.RecommendedGenreResponse;
import com.example.demo.domain.recommendation.entity.UserPlaylistHistory;
import com.example.demo.domain.recommendation.repository.UserPlaylistHistoryRepository;
import com.example.demo.domain.representative.entity.RepresentativePlaylist;
import com.example.demo.domain.representative.repository.RepresentativeRepresentativePlaylistRepository;
import com.example.demo.domain.song.entity.Song;
import com.example.demo.domain.song.repository.SongRepository;
import com.example.demo.domain.user.entity.Users;
import com.example.demo.domain.user.repository.UsersRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaylistMainPageServiceImpl implements PlaylistMainPageService {

    private final PlaylistRepository playlistRepository;
    private final UsersRepository userRepository;
    private final UserPlaylistHistoryRepository userPlaylistHistoryRepository;
    private final SongRepository songRepository;
    private final CdService cdService;
    private final RepresentativeRepresentativePlaylistRepository representativePlaylistRepository;

    private static final int RECOMMENDATION_LIMIT = 3;

    @Override
    @Transactional(readOnly = true)
    public MainPlaylistDetailResponse getPlaylistDetail(Long playlistId, String userId) {
        // 대표 플레이리스트만 조회
        var playlist = playlistRepository.findById(playlistId)
                .filter(Playlist::isRepresentative)
                .orElseThrow(() -> new IllegalArgumentException("대표 플레이리스트를 찾을 수 없습니다."));

        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<Song> songs = songRepository.findSongsByPlaylistId(playlist.getId());
        List<SongDto> songDtos = songs.stream()
                .map(SongDto::from)
                .toList();
        // 재생 기록 저장
        userPlaylistHistoryRepository.save(UserPlaylistHistory.of(user, playlist));
        // 방문 수 증가
        playlistRepository.incrementVisitCount(playlist.getId());

        var cdResponse = cdService.getOnlyCdByPlaylistId(playlistId);
        return MainPlaylistDetailResponse.from(
                playlist,
                songDtos,
                cdResponse
        );
    }


    /*
    첫번째 추천 알고리즘
     */
    @Override
    public List<PlaylistCardResponse> getRecommendations(String userId) {
        List<Playlist> basePlaylists;

        List<Playlist> genreBased = userPlaylistHistoryRepository.findByUserRecentGenre(userId, 3);

        if (genreBased.isEmpty()) {
            basePlaylists = representativePlaylistRepository.findByVisitCount(6);
        } else {
            List<Playlist> visitCountTop3 = representativePlaylistRepository.findByVisitCount(3);
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
                .map(p -> PlaylistCardResponse.from(p, songMap.getOrDefault(p.getId(), List.of()), cdService.getOnlyCdByPlaylistId(p.getId())))
                .toList();
    }


    /*
    두번째 추천 알고리즘
     */
    @Override
    public List<PlaylistCardResponse> recommendFromLikedPlaylists(String myUserId) {
        // 1. 내가 팔로우한 유저들의 대표 플레이리스트 ID 조회
        List<Long> basePlaylistIds = playlistRepository.findFollowedRepresentativePlaylistIds(myUserId);

        List<Playlist> resultPlaylists;

        if (basePlaylistIds.isEmpty()) {
            resultPlaylists = playlistRepository.findLatestRepresentativePlaylists(myUserId, List.of(),
                    RECOMMENDATION_LIMIT);
        } else {
            // 2. 유사한 곡 기반 추천
            List<Playlist> basePlaylists = playlistRepository.findPlaylistsBySimilarSongs(
                    basePlaylistIds, myUserId, basePlaylistIds, RECOMMENDATION_LIMIT
            );

            // 3. 부족하면 fallback으로 채우기
            int remain = RECOMMENDATION_LIMIT - basePlaylists.size();
            if (remain > 0) {
                List<Long> excludeIds = new ArrayList<>(basePlaylistIds);
                excludeIds.addAll(basePlaylists.stream().map(Playlist::getId).toList());

                List<Playlist> fallback = playlistRepository.findLatestRepresentativePlaylists(
                        myUserId,
                        excludeIds,
                        remain
                );

                basePlaylists.addAll(fallback);
            }

            resultPlaylists = basePlaylists;
        }

        // 4. 곡 매핑
        List<Long> ids = resultPlaylists.stream().map(Playlist::getId).toList();
        Map<Long, List<Song>> songMap = songRepository.findAllByPlaylistIdIn(ids).stream()
                .collect(Collectors.groupingBy(song -> song.getPlaylist().getId()));

        // 5. 응답 매핑
        return resultPlaylists.stream()
                .map(p -> PlaylistCardResponse.from(p, songMap.getOrDefault(p.getId(), List.of()),cdService.getOnlyCdByPlaylistId(p.getId())))
                .toList();
    }


    @Override
    public List<RecommendedGenreResponse> recommendGenres(String userId) {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        Set<PlaylistGenre> genres = new LinkedHashSet<>();

        // 0차: 최근 많이 들은 장르 기반 플레이리스트 → 장르 추출
        List<Playlist> genreBased = userPlaylistHistoryRepository.findByUserRecentGenre(userId, 3);
        for (Playlist playlist : genreBased) {
            if (genres.size() >= 5)
                break;
            genres.add(playlist.getGenre());
        }

        // 1차: 어제 가장 많이 들은 장르
        List<PlaylistGenre> topGenres = userPlaylistHistoryRepository.findTopGenresByDate(yesterday);
        for (PlaylistGenre genre : topGenres) {
            if (genres.size() >= 5)
                break;
            genres.add(genre);
        }

        // 2차: 누적 기준 유저가 자주 들은 장르
        List<PlaylistGenre> userGenres = userPlaylistHistoryRepository.findMostPlayedGenresByUser(userId);
        for (PlaylistGenre genre : userGenres) {
            if (genres.size() >= 5)
                break;
            genres.add(genre);
        }

        // 3차: 전체 장르에서 부족한 것 보완
        for (PlaylistGenre genre : PlaylistGenre.values()) {
            if (genres.size() >= 5)
                break;
            genres.add(genre);
        }

        // 응답으로 매핑
        List<RecommendedGenreResponse> result = new ArrayList<>();
        for (PlaylistGenre genre : genres) {
            if (result.size() >= 5)
                break;
            result.add(RecommendedGenreResponse.from(genre));
        }

        return result;
    }
}