package com.example.demo.domain.songs.repository;

import com.example.demo.domain.songs.entity.Song;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface SongRepository extends ReactiveCrudRepository<Song, Long> {

    /**
     * 특정 플레이리스트에 속한 모든 노래 조회
     */
    Flux<Song> findAllByPlaylistId(Long playlistId);

    Flux<Song> deleteAllByPlaylistId(Long playlistId);

    // 필요한 경우 추가 쿼리 메서드 선언 가능
}

