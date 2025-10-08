package com.example.demo.domain.like.entity;

import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.user.entity.Users;
import com.example.demo.global.time.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "likes",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"users_id", "playlist_id"})
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

    public Likes(Users users, Playlist playlist) {
        this.users = users;
        this.playlist = playlist;
    }
}