package com.example.demo.domain.follow.service;

import com.example.common.error.code.UserErrorCode;
import com.example.common.error.exception.UserException;
import com.example.demo.domain.follow.repository.FollowRepository;
import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.playlist.repository.PlaylistRepository;
import com.example.demo.domain.user.entity.Users;
import com.example.demo.domain.user.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlaylistFollowService {

    private final UsersRepository usersRepository;
    private final PlaylistRepository playlistRepository;
    private final FollowRepository playlistFollowRepository;

    public void follow(String userId, Long playlistId) {
        Users me = usersRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new IllegalArgumentException("플레이리스트가 존재하지 않습니다. id=" + playlistId));

        boolean exists = playlistFollowRepository.existsByUsersIdAndPlaylistId(userId, playlistId);
        if (exists) {
            return; // 이미 팔로우 상태면 아무 것도 하지 않음
        }
        playlistFollowRepository.insertIfNotExists(me.getId(), playlist.getId());
    }


    public void unfollow(String userId, Long playlistId) {
        // 존재 검증(유저/플리) — 필요 없다면 생략 가능
        if (!usersRepository.existsById(userId)) {
            throw new UserException(UserErrorCode.USER_NOT_FOUND);
        }
        if (!playlistRepository.existsById(playlistId)) {
            throw new IllegalArgumentException("플레이리스트가 존재하지 않습니다. id=" + playlistId);
        }

        boolean exists = playlistFollowRepository.existsByUsersIdAndPlaylistId(userId, playlistId);
        if (exists) {
            playlistFollowRepository.deleteByUsersIdAndPlaylistId(userId, playlistId);
        }
    }

}
