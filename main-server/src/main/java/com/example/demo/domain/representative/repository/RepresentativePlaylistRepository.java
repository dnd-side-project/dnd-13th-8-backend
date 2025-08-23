package com.example.demo.domain.representative.repository;

import com.example.demo.domain.playlist.dto.PlaylistGenre;
import com.example.demo.domain.representative.entity.RepresentativePlaylist;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RepresentativePlaylistRepository extends JpaRepository<RepresentativePlaylist, Long> {
    Optional<RepresentativePlaylist> findByUser_Id(String usersId);

    boolean existsByUser_Id(String userId);

    @Query("""
        SELECT rp
        FROM RepresentativePlaylist rp
        JOIN FETCH rp.playlist p
        WHERE p.genre = :genre
        ORDER BY p.visitCount DESC
        """)
    List<RepresentativePlaylist> findByGenreOrderByVisitCountDesc(@Param("genre") PlaylistGenre genre, Pageable pageable);

    @Query("""
        SELECT rp
        FROM RepresentativePlaylist rp
        JOIN FETCH rp.playlist p
        WHERE p.genre = :genre
        ORDER BY p.createdAt DESC
        """)
    List<RepresentativePlaylist> findByGenreOrderByCreatedAtDesc(@Param("genre") PlaylistGenre genre, Pageable pageable);
}
