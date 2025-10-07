package com.example.demo.domain.playlist.service;

import com.example.common.error.code.PlaylistErrorCode;
import com.example.common.error.code.UserErrorCode;
import com.example.common.error.exception.PlaylistException;
import com.example.common.error.exception.UserException;
import com.example.demo.domain.cd.dto.request.CdItemRequest;
import com.example.demo.domain.cd.repository.CdRepository;
import com.example.demo.domain.cd.service.CdService;
import com.example.demo.domain.playlist.dto.playlistdto.MainPlaylistDetailResponse;
import com.example.demo.domain.playlist.dto.SongDto;
import com.example.demo.domain.playlist.dto.playlistdto.PlaylistCreateRequest;
import com.example.demo.domain.playlist.dto.playlistdto.PlaylistWithSongsResponse;
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
    private final PlaylistSaveService playlistSaveService;
    private final UsersRepository userRepository;
    private final UserPlaylistHistoryRepository userPlaylistHistoryRepository;
    private final SongRepository songRepository;
    private final CdService cdService;
    private final CdRepository cdRepository;

    @Override
    @Transactional
    public MainPlaylistDetailResponse getPlaylistDetail(Long playlistId, String userId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .filter(Playlist::isPublic)
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

    @Override
    @Transactional
    public PlaylistWithSongsResponse saveFinalPlaylistWithSongsAndCd(String usersId, PlaylistCreateRequest request,
                                                                     List<CdItemRequest> cdItemRequestList) {

        PlaylistWithSongsResponse response = playlistSaveService.savePlaylistWithSongs(usersId, request);

        cdService.saveCdItemList(response.playlistId(), cdItemRequestList);

        return response;
    }

    @Override
    @Transactional
    public PlaylistWithSongsResponse editFinalPlaylistWithSongsAndCd(String usersId, Long playlistId, PlaylistCreateRequest request,
                                                                     List<CdItemRequest> cdItemRequestList) {
        PlaylistWithSongsResponse response = playlistSaveService.editPlaylistWithSongs(usersId, playlistId, request);
        cdService.replaceCdItemList(playlistId, cdItemRequestList);

        return response;
    }

    @Override
    @Transactional
    public void deletePlaylist(String userId, Long playlistId) {
        // 1. 삭제 대상 검증
        Playlist toDelete = playlistRepository.findByIdAndUsers_Id(playlistId, userId)
                .orElseThrow(() -> new PlaylistException(
                        "해당 플레이리스트가 존재하지 않거나 권한이 없습니다.",
                        PlaylistErrorCode.PLAYLIST_NOT_FOUND
                ));

        // 2. 유저가 가진 플리 개수 확인
        long totalCount = playlistRepository.countByUserIdNative(userId);
        if (totalCount <= 1) {
            throw new PlaylistException(
                    "플레이리스트는 최소 1개 이상 존재해야 합니다.",
                    PlaylistErrorCode.PLAYLIST_NOT_FOUND
            );
        }

        //  4. 참조 테이블 순차 삭제 (중요!)
        cdRepository.deleteByPlaylistId(playlistId); // CD 테이블
        songRepository.deleteByPlaylistId(playlistId); // 곡
        userPlaylistHistoryRepository.deleteByPlaylistId(playlistId); // 재생기록

        // 5. 플레이리스트 삭제
        playlistRepository.delete(toDelete);
    }

}
