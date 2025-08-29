package com.example.demo.domain.playlist.repository;

import com.example.demo.domain.playlist.dto.PlaylistGenre;
import com.example.demo.domain.playlist.dto.PlaylistSortOption;
import com.example.demo.domain.playlist.entity.Playlist;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlaylistRepository extends JpaRepository<Playlist, Long>, PlaylistRepositoryCustom {

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

    @Query(value = "SELECT COUNT(*) FROM playlist WHERE user_id = :userId", nativeQuery = true)
    long countByUserIdNative(@Param("userId") String userId);

    // 기존 대표 플레이리스트 해제(벌크연산)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Playlist p SET p.isRepresentative = false " +
            "WHERE p.users.id = :userId AND p.isRepresentative = true")
    int clearRepresentativeByUserId(@Param("userId") String userId);


    @Query("""
    SELECT p
    FROM Playlist p
    WHERE p.users.id = :userId
      AND p.id <> :excludedId
    ORDER BY p.createdAt DESC
""")
    Optional<Playlist> findMostRecentExcluding(@Param("userId") String userId, @Param("excludedId") Long excludedId);


    @Query("""
        SELECT p
        FROM Playlist p
        WHERE p.users.id = :userId
          AND p.id <> :excludeId
        ORDER BY p.createdAt DESC
    """)
    Optional<Playlist> findNextRecent(String userId, Long excludeId);

    @Modifying
    @Query("update Playlist p set p.visitCount = p.visitCount + 1 where p.id = :id")
    int incrementVisitCount(@Param("id") Long id);


    //Optional<Playlist> findTopByGenreOrderByVisitCountDesc(PlaylistGenre genre);
}
