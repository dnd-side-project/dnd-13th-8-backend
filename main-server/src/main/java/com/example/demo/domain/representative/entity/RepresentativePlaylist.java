package com.example.demo.domain.representative.entity;

import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.user.entity.Users;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id"}) // 한 유저당 하나의 대표 플리만
})
public class RepresentativePlaylist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playlist_id", nullable = false)
    private Playlist playlist;

    public RepresentativePlaylist(Users user, Playlist playlist) {
        this.user = user;
        this.playlist = playlist;
    }

    public void changePlaylist(Playlist newPlaylist) {
        this.playlist = newPlaylist;
    }
}
