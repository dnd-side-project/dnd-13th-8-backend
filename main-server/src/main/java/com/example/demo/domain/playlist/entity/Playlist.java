package com.example.demo.domain.playlist.entity;

import com.example.demo.domain.playlist.dto.PlaylistGenre;
import com.example.demo.domain.user.entity.Users;
import com.example.demo.global.time.BaseTimeEntity;
import jakarta.persistence.*;
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

    @Column
    private boolean isPublic = true;

    /** 조회수 증가 */
    public void increaseVisitCount() {
        this.visitCount += 1;
    }

    @Builder
    public Playlist(Users users, String name, Long visitCount, Boolean isPulic, PlaylistGenre genre) {
        this.genre = genre;
        this.name = name;
        this.visitCount = (visitCount != null) ? visitCount : 0L;
        this.isPublic = isPulic;
        this.users = users;
    }

    /**  공개로 변경 */
    public void updateIsPublic() {
        this.isPublic = !this.isPublic;
    }

    public void addVisitCount(long delta) {
        this.visitCount += delta;
    }

    public void editPlaylist(String name, PlaylistGenre genre, Boolean isPublic) {
        this.name = name;
        this.genre = genre;
        this.isPublic = isPublic;
    }


}
