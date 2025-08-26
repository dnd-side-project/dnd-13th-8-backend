package com.example.demo.domain.song.dto;


import com.example.demo.domain.playlist.dto.SongDto;
import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.song.entity.Song;
import com.example.demo.domain.song.util.DurationFormatUtil;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

public class SongMapper {

    /**
     * YouTubeVideoInfoDto + playlistId → Song 엔티티로 변환
     */
    public static Song toEntity(YouTubeVideoInfoDto dto, Playlist playlist) {
        return Song.builder()
                .playlist(playlist)
                .youtubeUrl(dto.link())
                .youtubeTitle(dto.title())
                .youtubeThumbnail(dto.thumbnailUrl())
                .youtubeLength(DurationFormatUtil.parseToSeconds(dto.duration()))
                .build();
    }


    /**
     * 저장된 Song 엔티티 → SongResponseDto로 변환
     */
    public static SongResponseDto toDto(Song song) {
        return new SongResponseDto(
                song.getId(),
                song.getPlaylist().getId(),
                song.getYoutubeUrl(),
                song.getYoutubeTitle(),
                song.getYoutubeThumbnail(),
                song.getYoutubeLength()
        );
    }

    public static List<SongDto> mapPreviewSongs(List<Song> songs) {
        return songs.stream()
                .map(song -> new SongDto(
                        song.getId(),
                        song.getYoutubeTitle(),
                        song.getYoutubeUrl(),
                        song.getYoutubeThumbnail(),
                        song.getYoutubeLength()
                ))
                .collect(Collectors.toList());
    }
}
