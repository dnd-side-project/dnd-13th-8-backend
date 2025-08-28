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
        // 커서 기준 조회 (hasNext 판단 위해 size + 1로 조회)
        List<BrowsePlaylistCard> cards = browsePlaylistRepository
                .findByUserIdWithCursorPaging(userId, cursorPosition, cursorCardId, size + 1);


        // 최초 요청이고 데이터가 전혀 없을 때 → fallback
        if (cards.isEmpty() && cursorPosition == null && cursorCardId == null) {

            int fallbackId = getRandomFallbackId();

            List<BrowsePlaylistCard> fallbackCards = browsePlaylistRepository.findDistinctByPlaylistIdWithinPosition(fallbackId, userId, size);

            if (!fallbackCards.isEmpty()) {

                List<BrowsePlaylistDto> dtoList = fallbackCards.stream()
                        .map(BrowsePlaylistDto::from)
                        .collect(Collectors.toList());

                BrowsePlaylistCursor cursor = new BrowsePlaylistCursor(0, 0L); // 기본 커서

                return new CursorPageResponse<>(
                        dtoList,
                        cursor,
                        dtoList.size(),
                        false,
                        0L
                );
            } else {
                return new CursorPageResponse<>(List.of(), null, 0, false, 0L);
            }
        }


        return CursorPageConverter.toCursorResponse(
                cards,
                size,
                BrowsePlaylistDto::from,
                dto -> {
                    BrowsePlaylistCursor cursor= new BrowsePlaylistCursor(dto.position(), dto.cardId());
                    return cursor;
                },
                0L);
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
