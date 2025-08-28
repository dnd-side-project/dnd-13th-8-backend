package com.example.demo.domain.browse.repository;

import com.example.demo.domain.browse.entity.BrowsePlaylistCard;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BrowsePlaylistRepository extends JpaRepository<BrowsePlaylistCard, Long>, BrowsePlaylistRepositoryCustom {
    void deleteByUserId(String userId);

    @Query("SELECT b FROM BrowsePlaylistCard b " +
            "WHERE b.position = :position AND b.userId <> :userId " +
            "ORDER BY b.id ASC")
    List<BrowsePlaylistCard> findByPositionAndUserIdNotOrderByIdAsc(
            @Param("position") int position,
            @Param("userId") String userId,
            Pageable pageable
    );



}
