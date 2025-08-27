package com.example.demo.domain.user.repository;

import com.example.demo.domain.playlist.dto.PlaylistSortOption;
import com.example.demo.domain.playlist.dto.search.UserSearchDto;
import java.util.List;

public interface UserRepositoryCustom {

     List<UserSearchDto> searchUsersByQueryWithOffset(
            String query,
            PlaylistSortOption sort,
            int offset,
            int limit
    );
}