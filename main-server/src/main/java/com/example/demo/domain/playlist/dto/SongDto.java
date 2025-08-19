package com.example.demo.domain.playlist.dto;

import com.example.demo.domain.song.entity.Song;
import javax.sound.midi.Track;
import lombok.Builder;

@Builder
public record SongDto(
        Long id,
        String title,
        String youtubeUrl
) {
    public static SongDto from(Song song) {
        return SongDto.builder()
                .id(song.getId())
                .title(song.getYoutubeTitle())
                .youtubeUrl(song.getYoutubeUrl())
                .build();
    }
}