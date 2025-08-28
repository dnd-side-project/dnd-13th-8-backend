package com.example.demo.domain.browse.repository;

import com.example.demo.domain.browse.entity.BrowsePlaylistCard;
import org.springframework.data.jpa.repository.JpaRepository;


public interface BrowsePlaylistRepository extends JpaRepository<BrowsePlaylistCard, Long>, BrowsePlaylistRepositoryCustom {
    void deleteByUserId(String userId);

    long countByUserId(String userId);
}
