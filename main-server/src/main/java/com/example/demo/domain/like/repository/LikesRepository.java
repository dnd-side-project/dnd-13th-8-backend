package com.example.demo.domain.like.repository;

import com.example.demo.domain.like.entity.Likes;
import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.user.entity.Users;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikesRepository extends JpaRepository<Likes, Long> {
    Optional<Likes> findByUsersAndPlaylist(Users user, Playlist playlist);
}
