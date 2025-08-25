package com.example.demo.domain.cd.entity;

import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.prop.entity.Prop;
import com.example.demo.global.time.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
public class Cd extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false )
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playlist_id")
    private Playlist playlist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prop_id")
    private Prop prop;

    @Column(name = "x_coordinate")
    private Long xCoordinate;

    @Column(name = "y_coordinate")
    private Long yCoordinate;

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
              Long xCoordinate, Long yCoordinate,
              Long height, Long width, Long scale, Long angle) {
        this.playlist = playlist;
        this.prop = prop;
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
        this.height = height;
        this.width = width;
        this.scale = scale;
        this.angle = angle;
    }
}
