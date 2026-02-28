package com.example.demo.domain.recommendation.repository;

import com.example.demo.domain.playlist.dto.common.PlaylistGenre;
import com.example.demo.domain.playlist.entity.Playlist;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface UserPlaylistHistoryRepositoryCustom {

    List<Playlist> findByUserRecentGenre(String userId, int limit);

    List<PlaylistGenre> findTopGenresByDate(LocalDate date);

    List<PlaylistGenre> findMostPlayedGenresByUser(String userId);

    List<Playlist> findWeeklyTopPlaylists(LocalDateTime since, int limit);

    List<Playlist> findLatestPlayedPlaylists(int limit);
}
