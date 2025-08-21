package com.example.demo.domain.playlist.dto;

public record PlaylistSearchResponse(
        Long id,
        String playlistName,
        String ownerNickname,
        Long visitCount
) {}
