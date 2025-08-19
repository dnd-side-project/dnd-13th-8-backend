package com.example.demo.domain.song.repository;

import com.example.demo.domain.song.entity.Song;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SongRepository extends JpaRepository<Song, Long> {

    List<Song> findByPlaylistId(Long playlistId);

}
