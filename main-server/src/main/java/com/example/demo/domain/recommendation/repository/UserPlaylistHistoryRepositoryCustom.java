package com.example.demo.domain.recommendation.repository;

import com.example.demo.domain.playlist.dto.PlaylistGenre;
import com.example.demo.domain.recommendation.dto.RecommendedPlaylistResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserPlaylistHistoryRepositoryCustom {

    List<RecommendedPlaylistResponse> findByUserRecentGenre(String userId, int limit);

    List<RecommendedPlaylistResponse> findByLikeCount(int limit);

    List<PlaylistGenre> findTopGenresByDate(LocalDate date);
    List<PlaylistGenre> findMostPlayedGenresByUser(String userId);

}
