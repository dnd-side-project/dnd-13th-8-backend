package com.example.demo.domain.playlist.repository.command;

import com.example.demo.domain.playlist.entity.Playlist;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    Optional<Playlist> findByIdAndUsers_Id(Long playlistId, String userId);

    @Modifying
    @Query("update Playlist p set p.visitCount = p.visitCount + 1 where p.id = :id")
    int incrementVisitCount(@Param("id") Long id);

    List<Playlist> findByIsPublicTrue();

}
