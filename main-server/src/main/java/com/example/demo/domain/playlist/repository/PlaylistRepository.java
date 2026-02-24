package com.example.demo.domain.playlist.repository;

import com.example.demo.domain.playlist.entity.Playlist;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlaylistRepository extends JpaRepository<Playlist, Long>, PlaylistRepositoryCustom {

    @Query("""
    SELECT p
    FROM Playlist p
    JOIN FETCH p.users u
    WHERE u.id = :userId
    ORDER BY p.visitCount DESC
    """)
    List<Playlist> findByUserIdPopular(@Param("userId") String userId);

    @Query("""
    SELECT p
    FROM Playlist p
    JOIN FETCH p.users u
    WHERE u.id = :userId
    ORDER BY p.id DESC
    """)
    List<Playlist> findByUserIdRecent(@Param("userId") String userId);

    Optional<Playlist> findByIdAndUsers_Id(Long playlistId, String userId);

    long countByUsers_Id(String userId);

    @Query(value = "SELECT * FROM playlist " +
            "WHERE user_id = :userId AND id <> :excludePlaylistId " +
            "ORDER BY created_at DESC LIMIT 1", nativeQuery = true)
    Optional<Playlist> findMostRecentExcluding(@Param("userId") String userId,
                                               @Param("excludePlaylistId") Long excludePlaylistId);

    @Modifying
    @Query("update Playlist p set p.visitCount = p.visitCount + 1 where p.id = :id")
    int incrementVisitCount(@Param("id") Long id);

}
