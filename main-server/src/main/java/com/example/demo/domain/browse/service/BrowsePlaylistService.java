package com.example.demo.domain.browse.service;

import com.example.common.error.code.PlaylistErrorCode;
import com.example.common.error.code.UserErrorCode;
import com.example.common.error.exception.PlaylistException;
import com.example.common.error.exception.UserException;
import com.example.demo.domain.browse.repository.BrowsePlaylistRepository;
import com.example.demo.domain.playlist.dto.playlistdto.CursorPageResponse;
import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.playlist.repository.PlaylistRepository;
import com.example.demo.domain.recommendation.entity.UserPlaylistHistory;
import com.example.demo.domain.recommendation.repository.UserPlaylistHistoryRepository;
import com.example.demo.domain.user.entity.Users;
import com.example.demo.domain.user.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BrowsePlaylistService {

    private final BrowsePlaylistRepository browsePlaylistRepository;
    private final BrowseViewCountService browseViewCountService;
    private final UsersRepository usersRepository;
    private final UserPlaylistHistoryRepository userPlaylistHistoryRepository;
    private final PlaylistRepository playlistRepository;

    @Transactional(readOnly = true)
    public CursorPageResponse<Long, Long> getShuffledPlaylistIds(
            String userId,
            Long cursorId,
            int size
    ) {
        int limit = size > 0 ? size : 20;
        int limitPlusOne = limit + 1;
        String seedKey = minuteSeedKey(); // "yyyy-MM-dd HH:mm" (Asia/Seoul)

        List<Long> rows = (cursorId == null)
                ? browsePlaylistRepository.findFirstPageIdsShuffledExcludeMine(userId, seedKey, limitPlusOne)
                : browsePlaylistRepository.findNextPageIdsShuffledExcludeMine(userId, seedKey, cursorId, limitPlusOne);

        boolean hasNext = rows.size() > limit;
        List<Long> page = hasNext ? rows.subList(0, limit) : rows;

        Long nextCursor = (hasNext && !page.isEmpty())
                ? page.get(page.size() - 1)
                : null;

        return new CursorPageResponse<>(
                page,           // content: List<Long>
                nextCursor,     // nextCursor: 마지막 id
                page.size(),
                hasNext,
                -1L
        );
    }

    private String minuteSeedKey() {
        return java.time.LocalDateTime.now(ZoneId.of("Asia/Seoul"))
                .truncatedTo(ChronoUnit.MINUTES)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    public void confirmAndLogPlayback(String id, Long playlistId) {
        browseViewCountService.confirmView(id, playlistId);

        Users user = usersRepository.findById(id)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new PlaylistException(
                        "플레이리스트가 존재하지 않습니다. id=" + playlistId,
                        PlaylistErrorCode.PLAYLIST_NOT_FOUND
                ));

        UserPlaylistHistory history = UserPlaylistHistory.of(user, playlist);
        userPlaylistHistoryRepository.save(history);
    }
}
