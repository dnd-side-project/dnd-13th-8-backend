package com.example.demo.domain.song.repository;

import com.example.demo.domain.song.entity.Song;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SongRepository extends JpaRepository<Song, Long> {

    @Query("SELECT s FROM Song s WHERE s.playlist.id = :playlistId ORDER BY s.youtubeLength ASC")
    List<Song> findSongsByPlaylistId(@Param("playlistId") Long playlistId);

    void deleteByPlaylistId(Long playlistId);

    List<Song> findByPlaylistId(Long id);
}
