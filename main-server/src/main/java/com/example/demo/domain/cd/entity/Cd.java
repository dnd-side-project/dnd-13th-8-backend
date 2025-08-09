package com.example.demo.domain.cd.entity;

import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.prop.entity.Prop;
import jakarta.persistence.*;

@Entity
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

    @Column(name = "angle")
    private Long angle;

}
