package com.example.demo.domain.like.service;

import com.example.common.error.code.PlaylistErrorCode;
import com.example.common.error.code.UserErrorCode;
import com.example.common.error.exception.PlaylistException;
import com.example.common.error.exception.UserException;
import com.example.demo.domain.like.repository.LikesRepository;
import com.example.demo.domain.playlist.repository.PlaylistRepository;
import com.example.demo.domain.user.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LikesService {

    private final LikesRepository likesRepository;
    private final UsersRepository usersRepository;
    private final PlaylistRepository playlistRepository;

    @Transactional
    public void like(String userId, Long playlistId) {
        if (!usersRepository.existsById(userId)) {
            throw new UserException(UserErrorCode.USER_NOT_FOUND);
        }
        if (!playlistRepository.existsById(playlistId)) {
            throw new PlaylistException(PlaylistErrorCode.PLAYLIST_NOT_FOUND);
        }

        likesRepository.insertIfNotExists(userId, playlistId);
    }

    @Transactional
    public void unlike(String userId, Long playlistId) {
        if (!usersRepository.existsById(userId)) {
            throw new UserException(UserErrorCode.USER_NOT_FOUND);
        }
        if (!playlistRepository.existsById(playlistId)) {
            throw new PlaylistException(PlaylistErrorCode.PLAYLIST_NOT_FOUND);
        }

        likesRepository.deleteByUsers_IdAndPlaylist_Id(userId, playlistId);
    }

    public boolean isPlaylistLiked(String userId, Long playlistId) {
        return likesRepository.existsByUsers_IdAndPlaylist_Id(userId, playlistId);
    }
}
