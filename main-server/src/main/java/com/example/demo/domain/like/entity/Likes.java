package com.example.demo.domain.like.entity;

import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.user.entity.Users;
import com.example.demo.global.time.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Entity
@Table(name = "user_like_playlist",
        uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "playlist_id"}) // user_id와 playlist_id가 unique해야함
})
@NoArgsConstructor
public class Likes extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users users;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playlist_id")
    private Playlist playlist;

    public Likes(Users user, Playlist playlist) {
        this.users = user;
        this.playlist = playlist;
    }
}