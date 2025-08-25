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
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional
public class BrowsePlaylistServiceImpl implements BrowsePlaylistService {

    private final BrowsePlaylistRepository browsePlaylistRepository;
    private final UserPlaylistHistoryRepository userPlaylistHistoryRepository;
    private final UsersRepository usersRepository;
    private final PlaylistRepository playlistRepository;
    private final BrowseViewCountService browseViewCountService;

    // 구현
    @Override
    public BrowseResponse getShuffledPlaylists(String userId) {
        List<BrowsePlaylistCard> cards = browsePlaylistRepository.findByUserIdOrderByPosition(userId);
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