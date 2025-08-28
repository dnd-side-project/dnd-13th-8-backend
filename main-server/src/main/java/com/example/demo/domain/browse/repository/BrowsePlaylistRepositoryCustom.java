package com.example.demo.domain.browse.repository;

import com.example.demo.domain.browse.entity.BrowsePlaylistCard;
import java.util.List;

public interface BrowsePlaylistRepositoryCustom {

    List<BrowsePlaylistCard> findByUserIdWithCursorPaging(
            String userId, Integer cursorPosition, Long cursorCardId, int size
    );

    List<BrowsePlaylistCard> findDistinctByPlaylistIdWithinPosition(int position, String userId, int limit
    );

    List<BrowsePlaylistCard> findFallbackWithinPositionAfter(
            int position, long afterCardId, String excludeUserId, int sizePlusOne
    ) ;
}
