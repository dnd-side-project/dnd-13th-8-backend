package com.example.demo.domain.playlist.controller;

import com.example.demo.domain.playlist.dto.PlaylistResponse;
import com.example.demo.domain.playlist.dto.PlaylistSortOption;
import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.playlist.repository.PlaylistRepository;
import com.example.demo.domain.playlist.service.PlaylistService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public  class PlaylistServiceImpl implements PlaylistService {

    private final PlaylistRepository playlistRepository;

    @Transactional
    @Override
    public List<PlaylistResponse> getMyPlaylistsSorted(String userId, PlaylistSortOption sortOption) {

        Playlist representative = playlistRepository.findRepresentativeByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("대표 플레이리스트가 존재하지 않습니다."));

        List<Playlist> rest = switch (sortOption) {
            case POPULAR -> playlistRepository.findByUserIdPopular(userId);
            case RECENT -> playlistRepository.findByUserIdRecent(userId);
        };

        List<PlaylistResponse> result = new ArrayList<>();
        result.add(PlaylistResponse.from(representative));
        result.addAll(rest.stream()
                .map(PlaylistResponse::from)
                .toList());

        return result;
    }

}
