package com.example.demo.domain.recommendation.repository;

import com.example.demo.domain.playlist.dto.PlaylistGenre;
import com.example.demo.domain.playlist.entity.Playlist;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface UserPlaylistHistoryRepositoryCustom {

    /*
    1 번째
     */
    List<Playlist> findByUserRecentGenre(String userId, int limit);


    /*
    2번째
     */
    List<Playlist> findRecommendedPlaylistsByUser(String userId, int limit);

    /*
    3번째
     */
    //전날(또는 특정 날짜)의 사용자 재생 기록을 기반으로 인기 장르 TOP 6 조회
    List<PlaylistGenre> findTopGenresByDate(LocalDate date);

    //사용자의 전체 재생 기록을 기반으로 개인이 가장 많이 들은 장르 순으로 조회
    List<PlaylistGenre> findMostPlayedGenresByUser(String userId);

}
