package com.example.demo.domain.playlist.dto.search;

import com.example.demo.domain.playlist.dto.SongDto;
import java.util.List;

public record PlaylistSearchDto(
        Long playlistId,
        String playlistName,
        String userId,
        String username,
        List<SongDto> songs
) implements SearchItem {

    @Override
    public String getType() {
        return "PLAYLIST";
    }
}
