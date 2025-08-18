package com.example.demo.domain.user.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false, length = 15)
    private String id;

    private String kakaoId;

    @Column(name = "username")
    private String username;

//    @Column(name = "nickname")
//    private String nickname;
//
//    @Column(name = "profile_url")
//    private String profileUrl;

    private boolean enabled;

    @Builder
    public Users(String kakaoId, String username, boolean enabled) {
        this.kakaoId = kakaoId;
        this.username = username;
        this.enabled = enabled;
    }
}
