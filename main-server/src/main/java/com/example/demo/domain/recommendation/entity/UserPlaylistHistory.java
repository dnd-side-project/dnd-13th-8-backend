package com.example.demo.domain.recommendation.entity;

import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.user.entity.Users;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
public class UserPlaylistHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Users user;

    private LocalDateTime playedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    private Playlist playlist;

    @Builder
    public UserPlaylistHistory(Users user,  Playlist playlist, LocalDateTime playedAt) {
        this.user = user;
        this.playedAt = playedAt;
        this.playlist = playlist;
    }
}
