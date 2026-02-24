package com.example.demo.domain.playlist.service;

import com.example.common.error.code.PlaylistErrorCode;
import com.example.common.error.code.UserErrorCode;
import com.example.common.error.exception.PlaylistException;
import com.example.common.error.exception.UserException;
import com.example.demo.domain.cd.dto.request.SaveCdRequest;
import com.example.demo.domain.cd.repository.CdRepository;
import com.example.demo.domain.cd.service.CdService;
import com.example.demo.domain.playlist.dto.save.PlaylistDraft;
import com.example.demo.domain.playlist.dto.common.PlaylistDetailWithCreatorResponse;
import com.example.demo.domain.playlist.dto.common.SongDto;
import com.example.demo.domain.playlist.dto.save.SavePlaylistRequest;
import com.example.demo.domain.playlist.dto.save.SavePlaylistResponse;
import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.playlist.event.PlaylistDeleteEvent;
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
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional
    public PlaylistDetailWithCreatorResponse playPlaylist(Long playlistId, String userId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .filter(Playlist::isPublic)
                .orElseThrow(() -> new PlaylistException("플레이리스트가 없거나 비공개 상태입니다.", PlaylistErrorCode.PLAYLIST_NOT_FOUND));

        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        List<Song> songs = songRepository.findSongsByPlaylistId(playlist.getId());
        List<SongDto> songDtos = songs.stream().map(SongDto::from).toList();

        userPlaylistHistoryRepository.save(UserPlaylistHistory.of(user, playlist));
        playlistRepository.incrementVisitCount(playlist.getId());

        var cdResponse = cdService.getOnlyCdByPlaylistId(playlistId);
        return PlaylistDetailWithCreatorResponse.from(playlist, songDtos, cdResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PlaylistDetailWithCreatorResponse getPlaylistDetail(Long playlistId, String userId) {

        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new PlaylistException(
                        "플레이리스트가 존재하지 않습니다.",
                        PlaylistErrorCode.PLAYLIST_NOT_FOUND
                ));

        if (!playlist.isPublic() && !playlist.getUsers().getId().equals(userId)) {
            throw new PlaylistException(
                    "비공개 플레이리스트입니다.",
                    PlaylistErrorCode.PLAYLIST_NOT_FOUND
            );
        }

        List<Song> songs = songRepository.findSongsByPlaylistId(playlist.getId());
        List<SongDto> songDtos = songs.stream()
                .map(SongDto::from)
                .toList();

        var cdResponse = cdService.getOnlyCdByPlaylistId(playlistId);

        return PlaylistDetailWithCreatorResponse.from(playlist, songDtos, cdResponse);
    }

    @Override
    @Transactional
    public String saveDraftPlaylist(PlaylistDraft playlistDraft) {

        return playlistSaveService.createDraft(playlistDraft.savePlaylistRequest(), playlistDraft.saveCdRequest());
    }

    @Override
    @Transactional
    public SavePlaylistResponse saveFinalPlaylist(String usersId, String draftId) {

        PlaylistDraft draft = playlistSaveService.loadDraft(draftId);
        SavePlaylistRequest savePlaylistRequest = draft.savePlaylistRequest();
        SaveCdRequest saveCdRequest = draft.saveCdRequest();

        SavePlaylistResponse response = playlistSaveService.savePlaylistWithSongs(usersId, savePlaylistRequest);

        cdService.saveCdItemList(response.playlistId(), saveCdRequest.cdItems());

        playlistSaveService.deleteDraft(draftId);

        return response;
    }

    @Override
    @Transactional
    public SavePlaylistResponse editFinalPlaylist(String usersId, Long playlistId, String draftId) {

        PlaylistDraft draft = playlistSaveService.loadDraft(draftId);
        SavePlaylistRequest savePlaylistRequest = draft.savePlaylistRequest();
        SaveCdRequest saveCdRequest = draft.saveCdRequest();

        SavePlaylistResponse response = playlistSaveService.editPlaylistWithSongs(usersId, playlistId,
                savePlaylistRequest);
        cdService.replaceCdItemList(playlistId, saveCdRequest.cdItems());

        playlistSaveService.deleteDraft(draftId);

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

        //  2. 참조 테이블 순차 삭제 (중요!)
        cdRepository.deleteByPlaylistId(playlistId); // CD 테이블
        songRepository.deleteByPlaylistId(playlistId); // 곡
        userPlaylistHistoryRepository.deleteByPlaylistId(playlistId); // 재생기록

        // 3. 플레이리스트 삭제
        playlistRepository.delete(toDelete);

        applicationEventPublisher.publishEvent(new PlaylistDeleteEvent(String.valueOf(playlistId)));
    }

    @Override
    @Transactional
    public void updateIsPublic(String userId, Long playlistId) {
        Playlist target = playlistRepository.findByIdAndUsers_Id(playlistId, userId)
                .orElseThrow(() ->new PlaylistException(
                        "해당 플레이리스트가 존재하지 않거나 권한이 없습니다.",
                        PlaylistErrorCode.PLAYLIST_NOT_FOUND));
        target.updateIsPublic();
    }
}
