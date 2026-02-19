package com.example.demo.domain.playlist.dto.save;

import com.example.demo.domain.cd.dto.request.SaveCdRequest;
import jakarta.validation.Valid;

public record PlaylistDraft(@Valid SavePlaylistRequest savePlaylistRequest,
                            SaveCdRequest saveCdRequest) {
}
