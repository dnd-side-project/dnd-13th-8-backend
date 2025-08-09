package com.example.demo.domain.song.entity;

import com.example.demo.domain.playlist.entity.Playlist;
import jakarta.persistence.*;

@Entity
public class Song {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false )
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playlist_id")
    private Playlist playlist;

    @Column(name = "youtube_url")
    private String youtubeUrl;

    @Column(name = "youtube_title")
    private String youtubeTitle;

    @Column(name = "youtube_thumbnail")
    private String youtubeThumbnail;

    @Column(name = "youtube_length") //유튜브 영상 길이 sec 단위
    private Long youtubeLength;

}
