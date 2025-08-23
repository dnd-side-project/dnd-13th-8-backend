package com.example.demo.domain.follow.repository;


import com.example.demo.domain.follow.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowRepository extends JpaRepository<Follow, Long>, FollowRepositoryCustom {
   // Optional<Follow> findByUsersAndPlaylist(Users user, Playlist playlist);
}
