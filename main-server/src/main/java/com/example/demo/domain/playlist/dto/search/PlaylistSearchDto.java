package com.example.demo.domain.playlist.dto.search;

public record PlaylistSearchDto(
        Long playlistId,
        String playlistName
) implements SearchItem {
    @Override
    public String getType() {
        return "PLAYLIST";
    }
}
