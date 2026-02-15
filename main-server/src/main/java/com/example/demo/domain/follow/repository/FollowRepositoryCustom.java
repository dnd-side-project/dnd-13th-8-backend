package com.example.demo.domain.follow.repository;

import com.example.demo.domain.follow.dto.response.FollowedPlaylist;
import com.example.demo.domain.playlist.dto.common.PlaylistSortOption;
import java.util.List;

public interface FollowRepositoryCustom {

    List<FollowedPlaylist> findFolloweePlaylistsWithMeta(String userId, PlaylistSortOption sort, int limit);


}
