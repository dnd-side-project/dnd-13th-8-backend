package com.example.demo.domain.songs.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("song")
public class Song {

    @Id
    private Long id;

    @Column("playlist_id")
    private Long playlistId; // JPA 없이 직접 연결

    @Column("youtube_url")
    private String youtubeUrl;

    @Column("youtube_title")
    private String youtubeTitle;

    @Column("youtube_thumbnail")
    private String youtubeThumbnail;

    @Column("youtube_length")
    private Long youtubeLength; // 초 단위

    @Column("order_index")
    private Long orderIndex;
}


