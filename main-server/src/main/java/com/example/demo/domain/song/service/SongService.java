package com.example.demo.domain.song.service;

import com.example.demo.domain.song.dto.SongsByPlaylist;
import com.example.demo.domain.song.entity.Song;
import com.example.demo.domain.song.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SongService {

    private final SongRepository songRepository;

    @Transactional(readOnly = true)
    public SongsByPlaylist findSongsByPlaylistIdsIn(List<Long> playlistIds) {
        if (playlistIds == null || playlistIds.isEmpty()) {
            return SongsByPlaylist.empty();
        }

        Map<Long, List<Song>> grouped = songRepository.findAllByPlaylistIdIn(playlistIds).stream()
                .collect(Collectors.groupingBy(song -> song.getPlaylist().getId()));

        return new SongsByPlaylist(grouped);
    }
}
