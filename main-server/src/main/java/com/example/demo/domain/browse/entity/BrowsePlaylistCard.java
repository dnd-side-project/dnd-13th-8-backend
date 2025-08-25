package com.example.demo.domain.browse.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "browse_playlist_card")
public class BrowsePlaylistCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int position;

    // 원본 Playlist 참조용 식별자 (조인 안 함)
    @Column(nullable = false)
    private Long playlistId;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false, length = 200)
    private String playlistTitle;

    // 장르 문자열(예: "JAZZ")
    @Column(nullable = false, length = 50)
    private String genre;

    // 작성자 스냅샷
    @Column(nullable = false, length = 64)
    private String creatorId;

    @Column(nullable = false, length = 100)
    private String creatorName;

    // 곡 미리보기: JSON 문자열
    @Lob
    @Column(columnDefinition = "TEXT")
    private String songsJson;

    // 대표 플레이리스트 여부
    @Column(nullable = false)
    private boolean isRepresentative;

    // 공유 URL
    @Column(length = 500)
    private String shareUrl;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String cdItemsJson;

    // 전체 재생 시간 문자열(예: "09:32")
    @Column(length = 16)
    private String totalTime;

    @Builder
    public BrowsePlaylistCard(
            Long playlistId,
            String userId,
            String playlistTitle,
            int position,
            String genre,
            String creatorId,
            String creatorName,
            String songsJson,
            boolean isRepresentative,
            String shareUrl,
            String cdItemsJson,
            String totalTime
    ) {
        this.playlistId = playlistId;
        this.userId = userId;
        this.playlistTitle = playlistTitle;
        this.position = position;
        this.genre = genre;
        this.creatorId = creatorId;
        this.creatorName = creatorName;
        this.songsJson = songsJson;
        this.isRepresentative = isRepresentative;
        this.shareUrl = shareUrl;
        this.cdItemsJson = cdItemsJson;
        this.totalTime = totalTime;
    }
}
