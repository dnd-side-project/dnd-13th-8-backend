package com.example.demo.domain.follow.repository;

import com.example.demo.domain.follow.dto.response.FollowListItem;
import com.example.demo.domain.follow.dto.response.FollowedPlaylist;
import com.example.demo.domain.playlist.dto.common.PlaylistSortOption;
import java.util.List;

public interface FollowRepositoryCustom {

    List<FollowedPlaylist> findFolloweePlaylistsWithMeta(String userId, PlaylistSortOption sort, int limit);

    List<FollowListItem> findFollowerListByUserId(String userId, Long cursor, int limit);
    List<FollowListItem> findFolloweeListByUserId(String userId, Long cursor, int limit);
    List<String> findFolloweeIdsIn(String userId, List<String> followeeIds);
}
