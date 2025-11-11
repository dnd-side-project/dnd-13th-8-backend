package com.example.demo.domain.cd.entity;

import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.prop.entity.Prop;
import com.example.demo.global.time.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Entity
@NoArgsConstructor
public class Cd extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false )
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "playlist_id")
    private Playlist playlist;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "prop_id")
    private Prop prop;

    @Column(name = "x_coordinate")
    private Long xCoordinate;

    @Column(name = "y_coordinate")
    private Long yCoordinate;

    @Column(name = "z_coordinate")
    private Long zCoordinate;

    @Column(name = "height")
    private Long height;

    @Column(name = "width")
    private Long width;

    @Column(name = "scale")
    private Long scale;

    @Column(name = "angle")
    private Long angle;

    @Builder
    public Cd(Playlist playlist, Prop prop,
              Long xCoordinate, Long yCoordinate, Long zCoordinate,
              Long height, Long width, Long scale, Long angle) {
        this.playlist = playlist;
        this.prop = prop;
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
        this.zCoordinate = zCoordinate;
        this.height = height;
        this.width = width;
        this.scale = scale;
        this.angle = angle;
    }
}
