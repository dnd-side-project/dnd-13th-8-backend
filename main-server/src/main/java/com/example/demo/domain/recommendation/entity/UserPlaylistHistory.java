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

/**
 *  회원이 언제 어떤 플레이리스트를 들었는가
 */
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

    private UserPlaylistHistory(Users user, Playlist playlist, LocalDateTime playedAt) {
        this.user = user;
        this.playlist = playlist;
        this.playedAt = playedAt;
    }

    public static UserPlaylistHistory of(Users user, Playlist playlist) {
        return new UserPlaylistHistory(user, playlist, LocalDateTime.now());
    }
}
