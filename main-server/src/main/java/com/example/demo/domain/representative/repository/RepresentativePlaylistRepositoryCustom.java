package com.example.demo.domain.representative.repository;


import com.example.demo.domain.playlist.dto.PlaylistGenre;
import com.example.demo.domain.playlist.dto.PlaylistSortOption;
import com.example.demo.domain.playlist.dto.search.PlaylistSearchDto;
import com.example.demo.domain.playlist.dto.search.UserSearchDto;
import com.example.demo.domain.playlist.entity.Playlist;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Pageable;

public interface RepresentativePlaylistRepositoryCustom {

    List<PlaylistSearchDto> searchRepresentativePlaylists(String query, PlaylistSortOption sort, Pageable pageable);

    List<UserSearchDto> searchUsersWithRepresentativePlaylist(String query, Pageable pageable);

    // (1번째)
    List<Playlist> findByVisitCount(int limit);

    //(3번 추천) 장르 목록에 대해 각 장르별로 방문 수가 가장 높은 대표 플레이리스트를 1개씩 반환
    List<Playlist> findTopVisitedRepresentativePlaylistsByGenres(Set<PlaylistGenre> genres);

}
