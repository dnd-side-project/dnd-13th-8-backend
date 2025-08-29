package com.example.demo.domain.playlist.service;

import com.example.common.error.code.PlaylistErrorCode;
import com.example.common.error.exception.PlaylistException;
import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.playlist.repository.PlaylistRepository;
import com.example.demo.domain.user.entity.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class PlaylistDeleteService {

    private final PlaylistRepository playlistRepository;
    private final PlaylistSaveService playlistSaveService;

    /**
     * 삭제된 대표 플레이리스트를 대체할 새 대표를 지정합니다.
     * 삭제된 플리 제외하고 가장 최근 플리를 새 대표로 설정합니다.
     */
    public void assignNewRepresentativeIfNecessary(String userId, Long deletedPlaylistId) {
        playlistRepository.findMostRecentExcluding(userId, deletedPlaylistId)
                .ifPresentOrElse(
                        newRepPlaylist -> {
                            Users user = newRepPlaylist.getUsers();
                            playlistSaveService.replaceRepresentativePlaylist(user, newRepPlaylist);
                        },
                        () -> {
                            throw new PlaylistException(PlaylistErrorCode.PLAYLIST_NOT_FOUND);
                        }
                );
    }
}
