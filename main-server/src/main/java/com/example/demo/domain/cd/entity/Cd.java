package com.example.demo.domain.cd.entity;

import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.prop.entity.Prop;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
public class Cd {
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

    @Column(name = "z_coordinate")
    private Long zCoordinate;

    @Column(name = "angle")
    private Long angle;

    @Builder
    public Cd(Playlist playlist, Prop prop,
              Long xCoordinate, Long yCoordinate,
              Long zCoordinate, Long angle) {
        this.playlist = playlist;
        this.prop = prop;
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
        this.zCoordinate = zCoordinate;
        this.angle = angle;
    }
}
