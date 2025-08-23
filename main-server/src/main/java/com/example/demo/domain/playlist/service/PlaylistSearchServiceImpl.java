package com.example.demo.domain.playlist.service;

import com.example.demo.domain.playlist.dto.PlaylistGenre;
import com.example.demo.domain.playlist.dto.PlaylistSearchResponse;
import com.example.demo.domain.playlist.dto.PlaylistSortOption;
import com.example.demo.domain.playlist.dto.search.CombinedSearchResponse;
import com.example.demo.domain.playlist.dto.search.PlaylistSearchDto;
import com.example.demo.domain.playlist.dto.search.SearchItem;
import com.example.demo.domain.playlist.dto.search.UserSearchDto;
import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.playlist.repository.PlaylistRepository;
import com.example.demo.domain.representative.entity.RepresentativePlaylist;
import com.example.demo.domain.representative.repository.RepresentativePlaylistRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlaylistSearchServiceImpl implements PlaylistSearchService {

    private final RepresentativePlaylistRepository representativePlaylistRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PlaylistSearchResponse> searchByGenre(PlaylistGenre genre, PlaylistSortOption sort, Integer limit) {
        int finalLimit = 10;
        if (limit != null && limit > 0 && limit <= 50) {
            finalLimit = limit;
        }

        Pageable pageable = PageRequest.of(0, finalLimit);

        List<RepresentativePlaylist> representatives = switch (sort) {
            case POPULAR -> representativePlaylistRepository
                    .findByGenreOrderByVisitCountDesc(genre, pageable);
            case RECENT -> representativePlaylistRepository
                    .findByGenreOrderByCreatedAtDesc(genre, pageable);
        };

        return representatives.stream()
                .map(rep -> {
                    Playlist p = rep.getPlaylist();
                    return new PlaylistSearchResponse(
                            p.getId(),
                            p.getName(),
                            p.getUsers().getUsername(),
                            p.getVisitCount()
                    );
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CombinedSearchResponse searchAll(String query, PlaylistSortOption sort, int limit) {
        Pageable pageable = Pageable.ofSize(limit);

        List<PlaylistSearchDto> playlists = representativePlaylistRepository
                .searchRepresentativePlaylists(query, sort, pageable);

        List<UserSearchDto> users = representativePlaylistRepository
                .searchUsersWithRepresentativePlaylist(query, pageable);

        List<SearchItem> combined = new ArrayList<>();
        combined.addAll(users);      // type = "USER"
        combined.addAll(playlists);  // type = "PLAYLIST"

        return new CombinedSearchResponse(combined);
    }


}
