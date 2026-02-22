package com.example.demo.domain.follow.repository;

import com.example.demo.domain.follow.dto.request.FollowSortOption;
import com.example.demo.domain.follow.dto.response.FollowListItem;
import com.example.demo.domain.follow.dto.response.FollowedPlaylist;
import com.example.demo.domain.playlist.dto.common.PlaylistSortOption;
import java.util.List;
import java.util.Optional;

public interface FollowRepositoryCustom {

    List<FollowedPlaylist> findFolloweePlaylistsWithMeta(String userId, PlaylistSortOption sort, int limit);

    Optional<FollowListItem> findMeInFolloweeList(String userId, String meId);
    Optional<FollowListItem> findMeInFollowerList(String userId, String meId);
    List<FollowListItem> findFollowerListByUserId(String userId, Long cursor, int limit, FollowSortOption sort, String excludeUserId);
    List<FollowListItem> findFolloweeListByUserId(String userId, Long cursor, int limit, FollowSortOption sort, String excludeUserId);
    List<String> findFolloweeIdsIn(String userId, List<String> followeeIds);
}
