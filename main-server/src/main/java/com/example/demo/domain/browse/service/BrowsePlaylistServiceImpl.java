package com.example.demo.domain.browse.service;

import com.example.common.error.code.PlaylistErrorCode;
import com.example.common.error.code.UserErrorCode;
import com.example.common.error.exception.PlaylistException;
import com.example.common.error.exception.UserException;
import com.example.demo.domain.browse.dto.BrowsePlaylistCursor;
import com.example.demo.domain.browse.dto.BrowsePlaylistDto;
import com.example.demo.domain.browse.entity.BrowsePlaylistCard;
import com.example.demo.domain.browse.repository.BrowsePlaylistRepository;
import com.example.demo.domain.playlist.dto.playlistdto.CursorPageResponse;
import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.playlist.repository.PlaylistRepository;
import com.example.demo.domain.recommendation.entity.UserPlaylistHistory;
import com.example.demo.domain.recommendation.repository.UserPlaylistHistoryRepository;
import com.example.demo.domain.user.entity.Users;
import com.example.demo.domain.user.repository.UsersRepository;
import com.example.demo.global.paging.CursorPageConverter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class BrowsePlaylistServiceImpl implements BrowsePlaylistService {

    private final BrowsePlaylistRepository browsePlaylistRepository;
    private final UserPlaylistHistoryRepository userPlaylistHistoryRepository;
    private final UsersRepository usersRepository;
    private final PlaylistRepository playlistRepository;
    private final BrowseViewCountService browseViewCountService;

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponse<BrowsePlaylistDto, BrowsePlaylistCursor> getShuffledPlaylists(
            String userId,
            Integer cursorPosition,
            Long cursorCardId,
            int size
    ) {
        boolean isFirstRequest = (cursorPosition == null && cursorCardId == null);

        List<BrowsePlaylistCard> cards = browsePlaylistRepository
                .findByUserIdWithCursorPaging(userId, cursorPosition, cursorCardId, size + 1);

        if (!cards.isEmpty()) {
            return getPlaylistsForExistingUser(cards, size);
        }

        long myCardCount = browsePlaylistRepository.countByUserId(userId);
        if (myCardCount == 0L) {
            boolean isValidCursor = (cursorPosition == null && cursorCardId == null)
                    || (cursorPosition != null && cursorCardId != null);
            if (!isValidCursor) {
                throw new IllegalArgumentException("신규 fallback 요청 시, cursorPosition과 cursorCardId는 둘 다 null이거나 둘 다 있어야 합니다.");
            }

            if (isFirstRequest) {
                return getPlaylistsForNewUser(userId, size);
            } else {
                return getPlaylistsForNewUserAfterCursor(userId, cursorPosition, cursorCardId, size);
            }
        }
        return new CursorPageResponse<>(List.of(), null, 0, false, 0L);
    }



    private CursorPageResponse<BrowsePlaylistDto, BrowsePlaylistCursor> getPlaylistsForExistingUser(
            List<BrowsePlaylistCard> cards,
            int size
    ) {
        return CursorPageConverter.toCursorResponse(
                cards,
                size,
                BrowsePlaylistDto::from,
                dto -> new BrowsePlaylistCursor(dto.position(), dto.cardId()),
                0L
        );
    }

    private CursorPageResponse<BrowsePlaylistDto, BrowsePlaylistCursor> getPlaylistsForNewUser(
            String userId,
            int size
    ) {
        int fallbackPosition = getRandomFallbackId();

        List<BrowsePlaylistCard> fallbackCards = browsePlaylistRepository
                .findDistinctByPlaylistIdWithinPosition(fallbackPosition, userId, size + 1);

        boolean hasNext;
        List<BrowsePlaylistCard> resultCards;

        if (fallbackCards.size() > size) {
            hasNext = true;
            resultCards = fallbackCards.subList(0, size);
        } else {
            hasNext = false;
            resultCards = fallbackCards;
        }

        List<BrowsePlaylistDto> dtoList = resultCards.stream()
                .map(BrowsePlaylistDto::from)
                .collect(Collectors.toList());

        BrowsePlaylistCursor cursor;
        if (!dtoList.isEmpty()) {
            BrowsePlaylistDto last = dtoList.get(dtoList.size() - 1);
            cursor = new BrowsePlaylistCursor(last.position(), last.cardId());
        } else {
            cursor = null;
        }

        return new CursorPageResponse<>(
                dtoList,
                cursor,
                dtoList.size(),
                hasNext,
                0L
        );
    }

    private CursorPageResponse<BrowsePlaylistDto, BrowsePlaylistCursor> getPlaylistsForNewUserAfterCursor(
            String userId,
            int position,
            long afterCardId,
            int size
    ) {
        List<BrowsePlaylistCard> fallbackCards = browsePlaylistRepository
                .findFallbackWithinPositionAfter(position, afterCardId, userId, size + 1);

        boolean hasNext;
        List<BrowsePlaylistCard> resultCards;

        if (fallbackCards.size() > size) {
            hasNext = true;
            resultCards = fallbackCards.subList(0, size);
        } else {
            hasNext = false;
            resultCards = fallbackCards;
        }

        List<BrowsePlaylistDto> dtoList = resultCards.stream()
                .map(BrowsePlaylistDto::from)
                .collect(Collectors.toList());

        BrowsePlaylistCursor cursor;
        if (!dtoList.isEmpty()) {
            BrowsePlaylistDto last = dtoList.get(dtoList.size() - 1);
            cursor = new BrowsePlaylistCursor(last.position(), last.cardId());
        } else {
            cursor = null;
        }

        return new CursorPageResponse<>(
                dtoList,
                cursor,
                dtoList.size(),
                hasNext,
                0L
        );
    }

    private int getRandomFallbackId() {
        List<Integer> ids = new ArrayList<>(List.of(0, 1, 2, 3, 4, 5));
        Collections.shuffle(ids, new Random(System.nanoTime()));
        return ids.getFirst();
    }

    @Override
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
