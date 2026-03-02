package com.example.demo.domain.song.dto;

import com.example.demo.domain.song.entity.Song;

import java.util.List;
import java.util.Map;

public record SongsByPlaylist(
        Map<Long, List<Song>> songsByPlaylistId
) {
    public static SongsByPlaylist empty() {
        return new SongsByPlaylist(Map.of());
    }

    public List<Song> songsOf(Long playlistId) {
        return songsByPlaylistId.getOrDefault(playlistId, List.of());
    }
}
