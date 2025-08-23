package com.example.demo.domain.recommendation.repository;

import com.example.demo.domain.playlist.dto.PlaylistGenre;
import com.example.demo.domain.playlist.dto.search.PlaylistSearchDto;
import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.recommendation.dto.RecommendedPlaylistResponse;
import java.time.LocalDate;
import java.util.List;

public interface UserPlaylistHistoryRepositoryCustom {

    /*
    1 번째
     */
    List<PlaylistSearchDto> findByUserRecentGenre(String userId, int limit);

    /*
    1 번째
     */
    List<PlaylistSearchDto> findByVisitCount(int limit);

    /*
    2번째
     */
    List<PlaylistSearchDto> findRecommendedPlaylistsByUser(String userId, int limit);

    /*
    3번째
     */
    List<PlaylistGenre> findTopGenresByDate(LocalDate date);

    List<PlaylistGenre> findMostPlayedGenresByUser(String userId);



}
