package com.example.demo.domain.playlist.repository;

import com.example.demo.domain.playlist.dto.PlaylistGenre;
import com.example.demo.domain.playlist.dto.PlaylistSortOption;
import com.example.demo.domain.playlist.entity.Playlist;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlaylistRepository extends JpaRepository<Playlist, Long>, PlaylistRecommendationRepositoryCustom {

    @Query("""
    SELECT p FROM Playlist p
    WHERE p.users.id = :userId
      AND p.isRepresentative = true
""")
    Optional<Playlist> findRepresentativeByUserId(@Param("userId") String userId);

    @Query("""
    SELECT p FROM Playlist p
    WHERE p.users.id = :userId
      AND p.isRepresentative = false
    ORDER BY p.visitCount DESC
""")
    List<Playlist> findByUserIdPopular(@Param("userId") String userId);

    @Query("""
    SELECT p FROM Playlist p
    WHERE p.users.id = :userId
      AND p.isRepresentative = false
    ORDER BY p.id DESC
""")
    List<Playlist> findByUserIdRecent(@Param("userId") String userId);

    Optional<Playlist> findByIdAndUsers_Id(Long playlistId, String userId);

    // 유저의 기존 플레이리스트 개수
    int countByUsers_Id(String userId);

    // 기존 대표 플레이리스트 해제
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Playlist p SET p.isRepresentative = false WHERE p.users.id = :userId AND p.isRepresentative = true")
    void clearPreviousRepresentative(String userId);

    boolean existsByShareCode(String shareCode);

    @Modifying
    @Query("update Playlist p set p.visitCount = p.visitCount + 1 where p.id = :id")
    int incrementVisitCount(@Param("id") Long id);

    @Query("""
        SELECT p
        FROM Playlist p
        WHERE p.genre = :genre
        ORDER BY
            CASE WHEN :sort = 'POPULAR' THEN p.visitCount END DESC,
            CASE WHEN :sort = 'RECENT' THEN p.createdAt END DESC
        """)
    List<Playlist> findByGenreSorted(
            @Param("genre") PlaylistGenre genre,
            @Param("sort") PlaylistSortOption sort,
            Pageable pageable
    );

    @Query("""
        SELECT p FROM Likes pl
        JOIN pl.playlist p
        WHERE pl.users.id = :userId
        ORDER BY 
            CASE WHEN :sort = 'POPULAR' THEN p.visitCount END DESC,
            CASE WHEN :sort = 'RECENT' THEN p.id END DESC
        """)
    List<Playlist> findLikedPlaylists(
            @Param("userId") String userId,
            @Param("sort") PlaylistSortOption sort,
            Pageable pageable
    );
}
