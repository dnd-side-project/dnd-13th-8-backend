package com.example.demo.domain.follow.repository;

import com.example.demo.domain.follow.dto.FollowPlaylistDto;
import com.example.demo.domain.playlist.dto.PlaylistSortOption;
import java.util.List;

public interface FollowRepositoryCustom {

    List<FollowPlaylistDto> findFolloweePlaylistsWithMeta(String userId, PlaylistSortOption sort, int limit);


}
