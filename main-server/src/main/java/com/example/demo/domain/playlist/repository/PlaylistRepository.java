package com.example.demo.domain.playlist.repository;

import com.example.demo.domain.playlist.dto.PlaylistSortOption;
import com.example.demo.domain.playlist.entity.Playlist;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

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



}
