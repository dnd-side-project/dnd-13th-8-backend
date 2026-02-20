package com.example.demo.domain.browse.service;

import com.example.common.error.code.PlaylistErrorCode;
import com.example.common.error.code.UserErrorCode;
import com.example.common.error.exception.PlaylistException;
import com.example.common.error.exception.UserException;
import com.example.demo.domain.browse.dto.BrowsePlaylistCursor;
import com.example.demo.domain.browse.dto.BrowsePlaylistDto;
import com.example.demo.domain.browse.dto.CreatorDto;
import com.example.demo.domain.browse.repository.BrowsePlaylistRepository;
import com.example.demo.global.paging.CursorPageResponse;
import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.playlist.repository.PlaylistRepository;
import com.example.demo.domain.playlist.service.PlaylistService;
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
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BrowsePlaylistService {

    private final BrowsePlaylistRepository browsePlaylistRepository;
    private final BrowseViewCountService browseViewCountService;
    private final UsersRepository usersRepository;
    private final UserPlaylistHistoryRepository userPlaylistHistoryRepository;
    private final PlaylistRepository playlistRepository;
    private final PlaylistService playlistService;

    @Transactional(readOnly = true)
    public CursorPageResponse<BrowsePlaylistDto, BrowsePlaylistCursor> getShuffledPlaylists(
            String userId,
            Long cursorId,  // 내부 커서는 playlistId
            int size
    ) {
        var idPage = getShuffledPlaylistIds(userId, cursorId, size);
        var ids = idPage.content();

        List<BrowsePlaylistDto> content = new ArrayList<>(ids.size());
        int basePos = 0; // position은 서버에서 무시하므로 임의 증가값이면 충분

        for (int i = 0; i < ids.size(); i++) {
            Long playlistId = ids.get(i);
            var detail = playlistService.getPlaylistDetail(playlistId, userId);

            BrowsePlaylistDto dto = new BrowsePlaylistDto(
                    playlistId,                 // cardId ←= playlistId 로 채움
                    basePos + i,                // position (프론트 형식용, 서버는 무시)
                    detail.playlistId(),
                    detail.playlistName(),
                    detail.genre().name(),
                    new CreatorDto(detail.creatorId(), detail.creatorNickname()),
                    detail.songs(),
                    true,
                    null,
                    (detail.cdResponse() != null) ? detail.cdResponse().cdItems() : List.of(),
                    null
            );
            content.add(dto);
        }

        BrowsePlaylistCursor nextCursor = null;
        if (idPage.hasNext() && !content.isEmpty()) {
            var last = content.get(content.size() - 1);
            // nextCursor.cardId 에도 마지막 playlistId 를 넣어줌 → 다음 요청에서 cursorId 로 사용됨
            nextCursor = new BrowsePlaylistCursor(last.position(), last.cardId());
        }

        return new CursorPageResponse<>(
                content,
                nextCursor,
                content.size(),
                idPage.hasNext(),
                0L
        );
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<Long, Long> getShuffledPlaylistIds(
            String userId,
            Long cursorId,
            int size
    ) {
        // #### 기존 로직 그대로 유지
        int limit = size > 0 ? size : 20;
        int limitPlusOne = limit + 1;
        String seedKey = minuteSeedKey();

        List<Long> rows = (cursorId == null)
                ? browsePlaylistRepository.findFirstPageIdsShuffledExcludeMine(userId, seedKey, limitPlusOne)
                : browsePlaylistRepository.findNextPageIdsShuffledExcludeMine(userId, seedKey, cursorId, limitPlusOne);

        boolean hasNext = rows.size() > limit;
        List<Long> page = hasNext ? rows.subList(0, limit) : rows;

        Long nextCursor = (hasNext && !page.isEmpty())
                ? page.get(page.size() - 1)
                : null;

        return new CursorPageResponse<>(
                page,
                nextCursor,
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
