package com.example.demo.domain.playlist.service;

import com.example.common.error.code.PlaylistErrorCode;
import com.example.common.error.code.UserErrorCode;
import com.example.common.error.exception.PlaylistException;
import com.example.common.error.exception.UserException;
import com.example.demo.domain.cd.service.CdService;
import com.example.demo.domain.playlist.dto.playlistdto.MainPlaylistDetailResponse;
import com.example.demo.domain.playlist.dto.SongDto;
import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.playlist.repository.PlaylistRepository;
import com.example.demo.domain.recommendation.entity.UserPlaylistHistory;
import com.example.demo.domain.recommendation.repository.UserPlaylistHistoryRepository;
import com.example.demo.domain.song.entity.Song;
import com.example.demo.domain.song.repository.SongRepository;
import com.example.demo.domain.user.entity.Users;
import com.example.demo.domain.user.repository.UsersRepository;

import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaylistServiceImpl implements PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final UsersRepository userRepository;
    private final UserPlaylistHistoryRepository userPlaylistHistoryRepository;
    private final SongRepository songRepository;
    private final CdService cdService;

    @Override
    @Transactional
    public MainPlaylistDetailResponse getPlaylistDetail(Long playlistId, String userId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .filter(Playlist::isRepresentative)
                .orElseThrow(() -> new PlaylistException("대표 플레이리스트를 찾을 수 없습니다.", PlaylistErrorCode.PLAYLIST_NOT_FOUND));

        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        List<Song> songs = songRepository.findSongsByPlaylistId(playlist.getId());
        List<SongDto> songDtos = songs.stream().map(SongDto::from).toList();

        userPlaylistHistoryRepository.save(UserPlaylistHistory.of(user, playlist));
        playlistRepository.incrementVisitCount(playlist.getId());

        var cdResponse = cdService.getOnlyCdByPlaylistId(playlistId);
        return MainPlaylistDetailResponse.from(playlist, songDtos, cdResponse);
    }
}
