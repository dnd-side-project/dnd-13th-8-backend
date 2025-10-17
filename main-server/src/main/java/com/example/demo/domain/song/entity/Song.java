package com.example.demo.domain.song.entity;

import com.example.demo.domain.playlist.entity.Playlist;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "song")
@NoArgsConstructor
public class Song {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="playlist_id", nullable=false)
    private Playlist playlist;

    private Long youtubeLength; // 초 단위

    private String youtubeUrl;

    private String youtubeTitle;

    private String youtubeThumbnail;

    private Long orderIndex; // 곡 순서

    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
    }

    @Builder
    public Song(Playlist playlist, Long youtubeLength, String youtubeUrl, String youtubeTitle,
                String youtubeThumbnail, Long orderIndex) {
        this.playlist = playlist;
        this.youtubeLength = youtubeLength;
        this.youtubeUrl = youtubeUrl;
        this.youtubeTitle = youtubeTitle;
        this.youtubeThumbnail = youtubeThumbnail;
        this.orderIndex = orderIndex;
    }
}

