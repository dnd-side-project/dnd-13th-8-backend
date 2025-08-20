package com.example.demo.domain.playlist.entity;

import com.example.demo.domain.playlist.dto.PlaylistGenre;
import com.example.demo.domain.user.entity.Users;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
public class Playlist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false )
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users users;

    @Column(name = "name")
    private String name;

    @Column(name = "visit_count")
    private Long visitCount = 0L;

    @Enumerated(EnumType.STRING)
    private PlaylistGenre genre;

    private Boolean isRepresentative;

    private String theme;

    @Builder
    public Playlist(Users users, String name, Long visitCount, Boolean isRepresentative, PlaylistGenre genre, String theme) {
        this.theme = theme;
        this.genre = genre;
        this.name = name;
        this.visitCount = visitCount;
        this.isRepresentative = isRepresentative;
        this.users = users;
    }
}
