package com.example.demo.domain.representative.repository;


import com.example.demo.domain.playlist.dto.PlaylistGenre;
import com.example.demo.domain.playlist.dto.PlaylistSortOption;
import com.example.demo.domain.playlist.dto.search.PlaylistSearchDto;
import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.representative.entity.RepresentativePlaylist;
import java.util.List;
import org.springframework.data.repository.query.Param;

public interface RepresentativePlaylistRepositoryCustom {

    // (1번째)
    List<Playlist> findByVisitCount(int limit);

    List<PlaylistSearchDto> searchPlaylistsByTitleWithOffset(String query, PlaylistSortOption sort, int offset, int limit);
    List<RepresentativePlaylist> findByGenreWithCursor(PlaylistGenre genre, PlaylistSortOption sort, Long cursorId, int limit);

}
