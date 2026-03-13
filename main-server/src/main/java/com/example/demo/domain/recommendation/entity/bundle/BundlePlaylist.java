package com.example.demo.domain.recommendation.entity.bundle;

import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.global.time.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Entity
@Table(
        name = "bundle_playlist",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"bundle_id", "playlist_id"})
        }
)
@NoArgsConstructor
public class BundlePlaylist extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "bundle_id", nullable = false)
    private Bundle bundle;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "playlist_id", nullable = false)
    private Playlist playlist;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    public BundlePlaylist(
            Bundle bundle,
            Playlist playlist,
            Integer displayOrder
    ) {
        this.bundle = bundle;
        this.playlist = playlist;
        this.orderIndex = displayOrder;
    }
}