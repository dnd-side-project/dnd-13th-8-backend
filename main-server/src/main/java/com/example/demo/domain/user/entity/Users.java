package com.example.demo.domain.user.entity;

import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.recommendation.entity.UserPlaylistHistory;
import com.example.demo.global.time.BaseTimeEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Entity
@NoArgsConstructor
public class Users extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    private String id;

    private String kakaoId;

    @Column(name = "username")
    private String username;
//
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
