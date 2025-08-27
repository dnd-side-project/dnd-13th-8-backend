package com.example.demo.domain.representative.repository;

import com.example.demo.domain.playlist.dto.PlaylistGenre;
import com.example.demo.domain.playlist.dto.PlaylistSortOption;
import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.representative.entity.RepresentativePlaylist;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RepresentativeRepresentativePlaylistRepository extends JpaRepository<RepresentativePlaylist, Long> ,
        RepresentativePlaylistRepositoryCustom {
    Optional<RepresentativePlaylist> findByUser_Id(String usersId);

    boolean existsByUser_Id(String userId);

    @Query("""
    SELECT rp
    FROM RepresentativePlaylist rp
    JOIN FETCH rp.playlist p
    WHERE p.genre = :genre
      AND p.id < :cursorId
    ORDER BY p.visitCount DESC, p.id DESC
    """)
    List<RepresentativePlaylist> findByGenreWithCursorSortByVisit(
            @Param("genre") PlaylistGenre genre,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    @Query("""
    SELECT rp
    FROM RepresentativePlaylist rp
    JOIN FETCH rp.playlist p
    WHERE p.genre = :genre
      AND p.id < :cursorId
    ORDER BY p.createdAt DESC, p.id DESC
    """)
    List<RepresentativePlaylist> findByGenreWithCursorSortByRecent(
            @Param("genre") PlaylistGenre genre,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    @Query("""
    SELECT rp.playlist.id
    FROM RepresentativePlaylist rp
    """)
    List<Long> findAllPlaylistIds();

    @Query("""
    SELECT rp FROM RepresentativePlaylist rp
    WHERE LOWER(rp.playlist.name) LIKE LOWER(CONCAT('%', :query, '%'))
      AND rp.id < :cursorId
    ORDER BY
      CASE WHEN :sort = 'POPULAR' THEN rp.playlist.visitCount END DESC,
      CASE WHEN :sort = 'RECENT' THEN rp.playlist.createdAt END DESC,
      rp.id DESC
""")
    List<RepresentativePlaylist> searchByTitleWithCursor(String query, PlaylistSortOption sort, Long cursorId, Pageable pageable);
}
