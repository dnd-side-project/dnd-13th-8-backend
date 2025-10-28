package com.example.demo.domain.playlist.event;

import lombok.Builder;

@Builder
public record PlaylistDeleteEvent(String playlistId) {
}
