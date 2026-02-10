package com.example.demo.domain.playlist.dto;

import com.example.demo.domain.cd.dto.request.SaveCdRequest;
import com.example.demo.domain.playlist.dto.playlistdto.SavePlaylistRequest;
import jakarta.validation.Valid;

public record PlaylistDraft(@Valid SavePlaylistRequest savePlaylistRequest,
                            SaveCdRequest saveCdRequest) {
}
