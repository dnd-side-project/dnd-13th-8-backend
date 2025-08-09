package com.example.demo.domain.like.entity;

import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.user.entity.User;
import jakarta.persistence.*;

@Entity
@Table(name = "user_like_playlist",
        uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "playlist_id"}) // user_id와 playlist_id가 unique해야함
})
public class Like {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playlist_id")
    private Playlist playlist;

}