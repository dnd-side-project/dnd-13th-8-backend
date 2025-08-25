package com.example.demo.domain.representative.service;

import com.example.common.error.code.PlaylistErrorCode;
import com.example.common.error.exception.PlaylistException;
import com.example.demo.domain.representative.entity.RepresentativePlaylist;
import com.example.demo.domain.representative.repository.RepresentativePlaylistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RepresentativePlaylistService {

    private final RepresentativePlaylistRepository representativePlaylistRepository;

    public RepresentativePlaylist findRepresentativePlaylistByUserId(String userId) {
        return representativePlaylistRepository.findByUser_Id(userId)
                .orElseThrow(()-> new PlaylistException(PlaylistErrorCode.PLAYLIST_NOT_FOUND));
    }

}
