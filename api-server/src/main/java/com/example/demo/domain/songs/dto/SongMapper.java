package com.example.demo.domain.songs.dto;


import com.example.demo.domain.songs.entity.Song;
import java.time.Duration;

public class SongMapper {

    public static YouTubeVideoInfoDto toDto(YouTubeVideoResponse.Item item, String originalLink) {
        return new YouTubeVideoInfoDto(
                originalLink,
                item.snippet().title(),
                item.snippet().thumbnails().high().url(),
                formatDuration(item.contentDetails().duration())
        );
    }

    /**
     * YouTubeVideoInfoDto + playlistId → Song 엔티티로 변환
     */
    public static Song toEntity(YouTubeVideoInfoDto dto, Long playlistId) {
        return Song.builder()
                .playlistId(playlistId)
                .youtubeUrl(dto.link())
                .youtubeTitle(dto.title())
                .youtubeThumbnail(dto.thumbnailUrl())
                .youtubeLength(parseDurationToSeconds(dto.duration()))
                .build();
    }

    /**
     * 저장된 Song 엔티티 → SongResponseDto로 변환
     */
    public static SongResponseDto toDto(Song song) {
        return new SongResponseDto(
                song.getId(),
                song.getPlaylistId(),
                song.getYoutubeUrl(),
                song.getYoutubeTitle(),
                song.getYoutubeThumbnail(),
                song.getYoutubeLength()
        );
    }

    private static String formatDuration(String isoDuration) {
        Duration duration = Duration.parse(isoDuration);
        long minutes = duration.toMinutes();
        long seconds = duration.minusMinutes(minutes).getSeconds();
        return String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * 문자열 형태의 영상 길이 ("03:22" 또는 "01:03:22") → 초 단위 long 변환
     */
    private static Long parseDurationToSeconds(String duration) {
        if (duration == null || duration.isBlank()) return 0L;

        String[] parts = duration.split(":");
        try {
            if (parts.length == 3) {
                return Long.parseLong(parts[0]) * 3600 +
                        Long.parseLong(parts[1]) * 60 +
                        Long.parseLong(parts[2]);
            } else if (parts.length == 2) {
                return Long.parseLong(parts[0]) * 60 +
                        Long.parseLong(parts[1]);
            } else {
                return 0L;
            }
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
