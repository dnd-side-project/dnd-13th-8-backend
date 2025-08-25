package com.example.demo.domain.browse.repository;

import com.example.demo.domain.browse.entity.BrowsePlaylistCard;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BrowsePlaylistRepository extends JpaRepository<BrowsePlaylistCard, Long> {
    void deleteByUserId(String userId);

    List<BrowsePlaylistCard> findByUserIdOrderByPosition(String userId);
}
