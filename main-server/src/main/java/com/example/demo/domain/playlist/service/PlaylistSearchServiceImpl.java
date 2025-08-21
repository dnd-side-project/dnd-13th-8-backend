package com.example.demo.domain.playlist.service;

import com.example.demo.domain.playlist.dto.PlaylistGenre;
import com.example.demo.domain.playlist.dto.PlaylistSearchResponse;
import com.example.demo.domain.playlist.dto.PlaylistSortOption;
import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.playlist.repository.PlaylistRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlaylistSearchServiceImpl implements PlaylistSearchService {

    private final PlaylistRepository playlistRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PlaylistSearchResponse> searchByGenre(PlaylistGenre genre, PlaylistSortOption sort, Integer limit) {
        int finalLimit = 10;
        if (limit != null && limit > 0 && limit <= 50) {
            finalLimit = limit;
        }

        Pageable pageable = PageRequest.of(0, finalLimit);
        List<Playlist> results = playlistRepository.findByGenreSorted(genre, sort, pageable);

        return results.stream()
                .map(p -> new PlaylistSearchResponse(
                        p.getId(),
                        p.getName(),
                        p.getUsers().getUsername(),
                        p.getVisitCount()
                ))
                .toList();
    }
}
