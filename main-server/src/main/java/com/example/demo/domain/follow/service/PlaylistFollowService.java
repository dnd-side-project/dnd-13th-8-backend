package com.example.demo.domain.follow.service;


import com.example.common.error.code.FollowErrorCode;
import com.example.common.error.code.PlaylistErrorCode;
import com.example.common.error.code.UserErrorCode;
import com.example.common.error.exception.FollowException;
import com.example.common.error.exception.PlaylistException;
import com.example.common.error.exception.UserException;
import com.example.demo.domain.follow.repository.FollowRepository;
import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.playlist.repository.PlaylistRepository;
import com.example.demo.domain.representative.repository.RepresentativePlaylistRepository;
import com.example.demo.domain.user.entity.Users;
import com.example.demo.domain.user.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaylistFollowService {

    private final UsersRepository usersRepository;
    private final PlaylistRepository playlistRepository;
    private final FollowRepository playlistFollowRepository;
    private final RepresentativePlaylistRepository representativePlaylistRepository;

    @Transactional
    public void follow(String userId, Long playlistId) {
        Users me = usersRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new PlaylistException(
                        "플레이리스트가 존재하지 않습니다. id=" + playlistId,
                        PlaylistErrorCode.PLAYLIST_NOT_FOUND
                ));

        // 대표 플레이리스트 여부 확인
        boolean isRepresentative = representativePlaylistRepository.existsByPlaylist_Id(playlistId);
        if (!isRepresentative) {
            throw new FollowException(FollowErrorCode.NOT_REPRESENTATIVE_PLAYLIST);
        }

        boolean exists = playlistFollowRepository.existsByUsersIdAndPlaylistId(userId, playlistId);
        if (exists) {
            return;
        }

        playlistFollowRepository.insertIfNotExists(me.getId(), playlist.getId());
    }

    @Transactional
    public void unfollow(String userId, Long playlistId) {
        if (!usersRepository.existsById(userId)) {
            throw new UserException(UserErrorCode.USER_NOT_FOUND);
        }

        if (!playlistRepository.existsById(playlistId)) {
            throw new PlaylistException(
                    "플레이리스트가 존재하지 않습니다. id=" + playlistId,
                    PlaylistErrorCode.PLAYLIST_NOT_FOUND
            );
        }

        boolean exists = playlistFollowRepository.existsByUsersIdAndPlaylistId(userId, playlistId);
        if (exists) {
            playlistFollowRepository.deleteByUsersIdAndPlaylistId(userId, playlistId);
        }
    }

    @Transactional
    public boolean isUserFollowing(String userId, Long playlistId) {
        return playlistFollowRepository.existsByUsersIdAndPlaylistId(userId, playlistId);
    }
}
