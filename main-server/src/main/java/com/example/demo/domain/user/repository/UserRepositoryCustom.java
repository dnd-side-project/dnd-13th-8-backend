package com.example.demo.domain.user.repository;

import com.example.demo.domain.playlist.dto.common.PlaylistSortOption;
import com.example.demo.domain.playlist.dto.search.SearchResult;
import com.example.demo.domain.playlist.dto.search.UserSearchDto;

public interface UserRepositoryCustom {

     SearchResult<UserSearchDto> searchUsersByQueryWithOffset(
            String query,
            PlaylistSortOption sort,
            int offset,
            int limit
    );
}