package com.example.demo.domain.playlist.service;

import com.example.demo.domain.like.entity.Likes;
import com.example.demo.domain.like.repository.LikesRepository;
import com.example.demo.domain.playlist.dto.GenreDto;
import com.example.demo.domain.playlist.dto.PlaylistDetailResponse;
import com.example.demo.domain.playlist.dto.PlaylistGenre;
import com.example.demo.domain.playlist.dto.PlaylistLikeResponse;
import com.example.demo.domain.playlist.dto.SongDto;
import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.playlist.repository.PlaylistRepository;
import com.example.demo.domain.playlist.recommendation.dto.PlaylistRecommendationResponse;
import com.example.demo.domain.playlist.recommendation.dto.RecommendedPlaylistCard;
import com.example.demo.domain.playlist.recommendation.entity.UserPlaylistHistory;
import com.example.demo.domain.playlist.recommendation.repository.UserPlaylistHistoryRepository;
import com.example.demo.domain.song.entity.Song;
import com.example.demo.domain.song.repository.SongRepository;
import com.example.demo.domain.user.entity.Users;
import com.example.demo.domain.user.repository.UsersRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
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
    private final LikesRepository likesRepository;

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

    @Override
    public PlaylistRecommendationResponse getRecommendations(String userId) {
        var genreBased = userPlaylistHistoryRepository.findByUserRecentGenre(userId, 3);
        if (genreBased.isEmpty()) {
            var visitCountTop6 = userPlaylistHistoryRepository.findByVisitCount(6);
            return PlaylistRecommendationResponse.onlyLikes(visitCountTop6);
        }

        var visitCountTop3 = userPlaylistHistoryRepository.findByVisitCount(3);
        return PlaylistRecommendationResponse.of(genreBased, visitCountTop3);
    }


    @Override
    public List<RecommendedPlaylistCard> recommendFromLikedPlaylists(String myUserId) {
        // 1. 추천용 플레이리스트 조회
        List<Playlist> recommendedPlaylists =
                playlistRepository.findRecommendedPlaylistsByUser(myUserId, RECOMMENDATION_LIMIT);

        // 2. for 문 기반으로 DTO 매핑
        List<RecommendedPlaylistCard> result = new ArrayList<>();

        for (Playlist playlist : recommendedPlaylists) {
            Users owner = playlist.getUsers();

            List<SongDto> songDtos = new ArrayList<>();
            for (Song song : playlist.getSongs()) {
                songDtos.add(new SongDto(
                        song.getId(),
                        song.getYoutubeTitle(),
                        song.getYoutubeThumbnail()
                ));
            }


            result.add(new RecommendedPlaylistCard(
                    playlist.getId(),
                    playlist.getName(),
                    owner.getUsername(),
                    playlist.getIsRepresentative(),
                    songDtos
            ));
        }
        return result;
    }

    @Override
    public List<GenreDto> recommendGenres(String userId) {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        // 1. 어제 전체 인기 장르 최대 6개 조회
        List<PlaylistGenre> topGenres = userPlaylistHistoryRepository.findTopGenresByDate(yesterday);

        // 2. 결과를 Set으로 중복 제거 및 순서 유지
        Set<PlaylistGenre> result = new LinkedHashSet<>(topGenres);

        // 3. 부족한 경우 내 재생 기록 기반 선호 장르로 보완
        if (result.size() < 6) {
            List<PlaylistGenre> userGenres = userPlaylistHistoryRepository.findMostPlayedGenresByUser(userId);
            for (PlaylistGenre genre : userGenres) {
                if (result.size() >= 6) break;
                result.add(genre);
            }
        }

        // 4. 그래도 부족하면 전체 장르 중 중복되지 않는 걸로 채움
        if (result.size() < 6) {
            for (PlaylistGenre genre : PlaylistGenre.values()) {
                if (result.size() >= 6) break;
                result.add(genre);
            }
        }

        // 5. 최종 6개만 잘라서 GenreDto로 변환
        return result.stream()
                .limit(6)
                .map(g -> new GenreDto(g.name(), g.getDisplayName()))
                .toList();
    }

    @Transactional
    @Override
    public PlaylistLikeResponse toggleLike(String userId, Long playlistId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new IllegalArgumentException("플레이리스트를 찾을 수 없습니다."));

        Optional<Likes> existing = likesRepository.findByUsersAndPlaylist(user, playlist);
        boolean liked;

        if (existing.isPresent()) {
            // 좋아요 취소
            likesRepository.delete(existing.get());
            liked = false;
        } else {
            // 좋아요 추가
            Likes like = new Likes(user, playlist);
            likesRepository.save(like);
            liked = true;
        }
        return new PlaylistLikeResponse(liked);
    }
}

