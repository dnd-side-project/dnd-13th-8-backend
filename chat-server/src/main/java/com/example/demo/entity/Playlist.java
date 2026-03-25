package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "playlist")
public class Playlist {

    @Id
    private Long id;

    @Column(name = "name")
    private String name;
}
