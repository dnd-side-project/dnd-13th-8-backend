package com.example.demo.domain.representative.repository;


import com.example.demo.domain.playlist.dto.PlaylistSortOption;
import com.example.demo.domain.playlist.dto.search.PlaylistSearchDto;
import com.example.demo.domain.playlist.dto.search.UserSearchDto;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface PlaylistSearchRepositoryCustom {

    List<PlaylistSearchDto> searchRepresentativePlaylists(String query, PlaylistSortOption sort, Pageable pageable);

    List<UserSearchDto> searchUsersWithRepresentativePlaylist(String query, Pageable pageable);
}
