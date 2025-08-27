package com.example.demo.domain.browse.service;

import com.example.common.error.code.UserErrorCode;
import com.example.common.error.exception.UserException;
import com.example.demo.domain.browse.dto.BrowsePlaylistDto;
import com.example.demo.domain.browse.dto.BrowseResponse;
import com.example.demo.domain.browse.entity.BrowsePlaylistCard;
import com.example.demo.domain.browse.repository.BrowsePlaylistRepository;
import com.example.demo.domain.browse.schedule.BrowsePlaylistShuffleService;
import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.playlist.repository.PlaylistRepository;
import com.example.demo.domain.recommendation.entity.UserPlaylistHistory;
import com.example.demo.domain.recommendation.repository.UserPlaylistHistoryRepository;
import com.example.demo.domain.user.entity.Users;
import com.example.demo.domain.user.repository.UsersRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
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
    public BrowseResponse getShuffledPlaylists(String userId) {
        List<BrowsePlaylistCard> cards = browsePlaylistRepository.findByUserIdOrderByPosition(userId);

        if (cards.isEmpty()) {
            log.info("셔플 카드 없음 → fallback 적용: userId={}", userId);

            // 1~5번 ID 셔플
            List<Long> fallbackIds = new ArrayList<>(List.of(1L, 2L, 3L, 4L, 5L));
            Collections.shuffle(fallbackIds, new Random(System.nanoTime()));

            // fallback 리스트
            List<BrowsePlaylistDto> fallbackDtos = new ArrayList<>();

            for (Long browsePlayId : fallbackIds) {
                browsePlaylistRepository.findById(browsePlayId).ifPresentOrElse(
                        browse -> fallbackDtos.add(BrowsePlaylistDto.from(browse)), // 변환 및 저장
                        () -> log.warn(" fallback playlistId={} 없음. 스킵", browsePlayId)
                );
            }

            return new BrowseResponse(fallbackDtos);
        }

        // 일반 셔플 카드 반환
        List<BrowsePlaylistDto> dtos = cards.stream()
                .map(BrowsePlaylistDto::from)
                .toList();

        return new BrowseResponse(dtos);
    }



    @Override
    public void confirmAndLogPlayback(String id, Long playlistId) {
        browseViewCountService.confirmView(id, playlistId);
        Users user = usersRepository.findById(id)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 플레이리스트입니다."));

        UserPlaylistHistory history = UserPlaylistHistory.of(user, playlist);
        userPlaylistHistoryRepository.save(history);
    }

}