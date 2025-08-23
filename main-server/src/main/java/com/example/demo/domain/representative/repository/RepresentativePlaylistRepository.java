package com.example.demo.domain.representative.repository;

import com.example.demo.domain.representative.entity.RepresentativePlaylist;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepresentativePlaylistRepository extends JpaRepository<RepresentativePlaylist, Long> {
    Optional<RepresentativePlaylist> findByUser_Id(String usersId);
}
