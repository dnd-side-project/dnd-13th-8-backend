package com.example.demo.domain.playlist.service.common;

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
import com.example.demo.domain.playlist.dto.save.SavePlaylistResponse;
import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.playlist.event.PlaylistDeleteEvent;
import com.example.demo.domain.playlist.repository.command.PlaylistRepository;
import com.example.demo.domain.recommendation.entity.UserPlaylistHistory;
import com.example.demo.domain.recommendation.repository.UserPlaylistHistoryRepository;
import com.example.demo.domain.song.entity.Song;
import com.example.demo.domain.song.repository.SongRepository;
import com.example.demo.domain.user.entity.Users;
import com.example.demo.domain.user.repository.UsersRepository;

import java.util.*;
import java.util.function.Function;

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
        Playlist playlist = getPublicPlaylist(playlistId);
        Users user = getUser(userId);

        PlaylistDetailWithCreatorResponse response = buildPlaylistDetailResponse(playlist);

        recordPlaylistPlay(user, playlist);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public PlaylistDetailWithCreatorResponse getPlaylistDetail(Long playlistId, String userId) {
        Playlist playlist = getAccessiblePlaylist(playlistId, userId);
        return buildPlaylistDetailResponse(playlist);
    }

    @Override
    @Transactional
    public String saveDraftPlaylist(PlaylistDraft playlistDraft) {
        return playlistSaveService.createDraft(
                playlistDraft.savePlaylistRequest(),
                playlistDraft.saveCdRequest()
        );
    }

    @Override
    @Transactional
    public SavePlaylistResponse saveFinalPlaylist(String usersId, String draftId) {
        return processDraftPlaylist(
                draftId,
                draft -> playlistSaveService.savePlaylistWithSongs(usersId, draft.savePlaylistRequest()),
                (playlistId, saveCdRequest) -> cdService.saveCdItemList(playlistId, saveCdRequest.cdItems())
        );
    }

    @Override
    @Transactional
    public SavePlaylistResponse editFinalPlaylist(String usersId, Long playlistId, String draftId) {
        return processDraftPlaylist(
                draftId,
                draft -> playlistSaveService.editPlaylistWithSongs(usersId, playlistId, draft.savePlaylistRequest()),
                (savedPlaylistId, saveCdRequest) -> cdService.replaceCdItemList(savedPlaylistId, saveCdRequest.cdItems())
        );
    }

    @Override
    @Transactional
    public void deletePlaylist(String userId, Long playlistId) {
        Playlist playlist = getOwnedPlaylist(playlistId, userId);

        deletePlaylistReferences(playlistId);
        playlistRepository.delete(playlist);
        publishPlaylistDeleteEvent(playlistId);
    }

    @Override
    @Transactional
    public void updateIsPublic(String userId, Long playlistId) {
        getOwnedPlaylist(playlistId, userId).updateIsPublic();
    }

    private Users getUser(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    }

    private Playlist getPublicPlaylist(Long playlistId) {
        return playlistRepository.findById(playlistId)
                .filter(Playlist::isPublic)
                .orElseThrow(() -> new PlaylistException(
                        "플레이리스트가 없거나 비공개 상태입니다.",
                        PlaylistErrorCode.PLAYLIST_NOT_FOUND
                ));
    }

    private Playlist getAccessiblePlaylist(Long playlistId, String userId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new PlaylistException(
                        "플레이리스트가 존재하지 않습니다.",
                        PlaylistErrorCode.PLAYLIST_NOT_FOUND
                ));

        validatePlaylistAccessible(playlist, userId);
        return playlist;
    }

    private Playlist getOwnedPlaylist(Long playlistId, String userId) {
        return playlistRepository.findByIdAndUsers_Id(playlistId, userId)
                .orElseThrow(() -> new PlaylistException(
                        "해당 플레이리스트가 존재하지 않거나 권한이 없습니다.",
                        PlaylistErrorCode.PLAYLIST_NOT_FOUND
                ));
    }

    private void validatePlaylistAccessible(Playlist playlist, String userId) {
        if (!playlist.isPublic() && !playlist.getUsers().getId().equals(userId)) {
            throw new PlaylistException(
                    "비공개 플레이리스트입니다.",
                    PlaylistErrorCode.PLAYLIST_NOT_FOUND
            );
        }
    }

    private List<Song> getSongs(Long playlistId) {
        return songRepository.findSongsByPlaylistId(playlistId);
    }

    private List<SongDto> toSongDtos(List<Song> songs) {
        return songs.stream()
                .map(SongDto::from)
                .toList();
    }

    private PlaylistDetailWithCreatorResponse buildPlaylistDetailResponse(Playlist playlist) {
        List<SongDto> songDtos = toSongDtos(getSongs(playlist.getId()));
        var cdResponse = cdService.getCdItemsByPlaylistId(playlist.getId());

        return PlaylistDetailWithCreatorResponse.from(playlist, songDtos, cdResponse);
    }

    private void recordPlaylistPlay(Users user, Playlist playlist) {
        userPlaylistHistoryRepository.save(UserPlaylistHistory.of(user, playlist));
        playlistRepository.incrementVisitCount(playlist.getId());
    }

    private SavePlaylistResponse processDraftPlaylist(
            String draftId,
            Function<PlaylistDraft, SavePlaylistResponse> playlistSaver,
            CdItemCommand cdItemCommand
    ) {
        PlaylistDraft draft = playlistSaveService.loadDraft(draftId);
        SavePlaylistResponse response = playlistSaver.apply(draft);

        cdItemCommand.execute(response.playlistId(), draft.saveCdRequest());
        playlistSaveService.deleteDraft(draftId);

        return response;
    }

    private void deletePlaylistReferences(Long playlistId) {
        cdRepository.deleteByPlaylistId(playlistId);
        songRepository.deleteByPlaylistId(playlistId);
        userPlaylistHistoryRepository.deleteByPlaylistId(playlistId);
    }

    private void publishPlaylistDeleteEvent(Long playlistId) {
        applicationEventPublisher.publishEvent(new PlaylistDeleteEvent(String.valueOf(playlistId)));
    }

    @FunctionalInterface
    private interface CdItemCommand {
        void execute(Long playlistId, SaveCdRequest saveCdRequest);
    }
}
