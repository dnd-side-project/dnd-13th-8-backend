package com.example.demo.domain.playlist.service;

import com.example.demo.domain.playlist.dto.PlaylistDetailResponse;
import com.example.demo.domain.playlist.dto.SongDto;
import com.example.demo.domain.playlist.repository.PlaylistRepository;
import com.example.demo.domain.recommendation.dto.PlaylistRecommendationResponse;
import com.example.demo.domain.recommendation.repository.UserPlaylistHistoryRepository;
import com.example.demo.domain.song.entity.Song;
import com.example.demo.domain.song.repository.SongRepository;
import com.example.demo.domain.user.repository.UsersRepository;
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
        user.play(playlist);

        // 2) 방문 수 증가
        playlistRepository.incrementVisitCount(playlistId);

        return PlaylistDetailResponse.from(playlist, songDtos);
    }

    @Override
    public PlaylistRecommendationResponse getRecommendations(String userId) {
        var genreBased = userPlaylistHistoryRepository.findByUserRecentGenre(userId, 3);
        if (genreBased.isEmpty()) {
            var visitCountTop6 = userPlaylistHistoryRepository.findByLikeCount(6);
            return PlaylistRecommendationResponse.onlyLikes(visitCountTop6);
        }

        var visitCountTop3 = userPlaylistHistoryRepository.findByLikeCount(3);
        return PlaylistRecommendationResponse.of(genreBased, visitCountTop3);
    }
}
