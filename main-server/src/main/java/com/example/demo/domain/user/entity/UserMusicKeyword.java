package com.example.demo.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserMusicKeyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users users;

    @Enumerated(EnumType.STRING)
    private MusicKeyword musicKeyword;

    public UserMusicKeyword(Users users, MusicKeyword musicKeyword) {
        this.users = users;
        this.musicKeyword = musicKeyword;
    }
}