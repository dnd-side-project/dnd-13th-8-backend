package com.example.demo.domain.playlist.entity;

import com.example.demo.domain.playlist.dto.PlaylistGenre;
import com.example.demo.domain.song.entity.Song;
import com.example.demo.domain.user.entity.Users;
import com.example.demo.global.time.BaseTimeEntity;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
public class Playlist extends BaseTimeEntity {
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

    @Column(nullable = false)
    private boolean isRepresentative = false;

    /** 조회수 증가 */
    public void increaseVisitCount() {
        this.visitCount += 1;
    }

    public void setIsRepresentative(boolean isRepresentative) {
        this.isRepresentative = isRepresentative;
    }

    @Builder
    public Playlist(Users users, String name, Long visitCount, Boolean isRepresentative, PlaylistGenre genre) {
        this.genre = genre;
        this.name = name;
        this.visitCount = visitCount;
        this.isRepresentative = isRepresentative;
        this.users = users;
    }

    /**  대표로 변경 */
    public void changeToRepresentative() {
        this.isRepresentative = true;
    }

    public void addVisitCount(long delta) {
        this.visitCount += delta;
    }

}
