package com.example.demo.domain.playlist.dto.search;

import com.example.demo.domain.playlist.dto.SongDto;
import java.util.List;

public record UserSearchDto(
        String userId,
        String username,
        Long playlistId,
        String playlistName,
        List<SongDto> tracks
) implements SearchItem {
    public String getType() { return "USER"; }
}
