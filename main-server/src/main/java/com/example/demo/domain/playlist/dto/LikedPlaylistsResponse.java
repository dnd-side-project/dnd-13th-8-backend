package com.example.demo.domain.playlist.dto;

import java.util.List;

public record LikedPlaylistsResponse (int size, List<LikedPlaylistDto> likedPlaylistDto){}
