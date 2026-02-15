package com.example.demo.domain.playlist.service;

import com.example.common.error.code.PlaylistErrorCode;
import com.example.common.error.code.UserErrorCode;
import com.example.common.error.exception.PlaylistException;
import com.example.common.error.exception.UserException;
import com.example.demo.domain.cd.service.CdService;
import com.example.demo.domain.follow.dto.response.FollowedPlaylist;
import com.example.demo.domain.follow.dto.response.FollowedPlaylistsResponse;
import com.example.demo.domain.follow.repository.FollowRepository;
import com.example.demo.domain.like.repository.LikesRepository;
import com.example.demo.domain.playlist.dto.common.SongDto;
import com.example.demo.domain.playlist.dto.common.PlaylistDetailWithCreatorResponse;
import com.example.demo.domain.playlist.dto.common.PlaylistDetailResponse;
import com.example.demo.domain.playlist.dto.common.PlaylistCoverResponse;
import com.example.demo.domain.playlist.dto.common.PlaylistSortOption;
import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.playlist.repository.PlaylistRepository;
import com.example.demo.domain.playlist.util.ShareCodeGenerator;
import com.example.demo.domain.song.entity.Song;
import com.example.demo.domain.song.repository.SongRepository;
import com.example.demo.domain.user.entity.Users;
import com.example.demo.domain.user.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaylistMyPageServiceImpl implements PlaylistMyPageService {

    private final PlaylistRepository playlistRepository;
    private final SongRepository songRepository;
    private final UsersRepository usersRepository;
    private final FollowRepository followRepository;
    private final LikesRepository likesRepository;

    private static final int DEFAULT_LIMIT = 20;
    private final CdService cdService;

    @Override
    @Transactional(readOnly = true)
    public List<PlaylistCoverResponse> getMyPlaylistsSorted(String userId, PlaylistSortOption sortOption) {
        List<Playlist> all = switch (sortOption) {
            case POPULAR -> playlistRepository.findByUserIdPopular(userId);
            case RECENT  -> playlistRepository.findByUserIdRecent(userId);
        };

        return all.stream()
                .map(p -> PlaylistCoverResponse.from(p,
                        cdService.getOnlyCdByPlaylistId(p.getId()),
                        likesRepository.existsByUsers_IdAndPlaylist_Id(userId, p.getId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlaylistCoverResponse> getLikedPlaylistsSorted(String userId, PlaylistSortOption sortOption) {
        List<Playlist> likedPlaylists = likesRepository.findLikedPlaylistsWithMeta(userId, sortOption, DEFAULT_LIMIT);

        return likedPlaylists.stream()
                .map(p -> PlaylistCoverResponse.from(p,
                        cdService.getOnlyCdByPlaylistId(p.getId()),
                        likesRepository.existsByUsers_IdAndPlaylist_Id(userId, p.getId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PlaylistDetailWithCreatorResponse getMyPlaylistDetail(String userId, Long playlistId) {
        Playlist playlist = playlistRepository.findByIdAndUsers_Id(playlistId, userId)
                .orElseThrow(() -> new PlaylistException("플레이리스트가 존재하지 않거나 권한이 없습니다.",
                        PlaylistErrorCode.PLAYLIST_NOT_FOUND));

        List<Song> songs = songRepository.findSongsByPlaylistId(playlistId);
        List<SongDto> songDtos = songs.stream().map(SongDto::from).toList();

        return PlaylistDetailWithCreatorResponse.from(playlist, songDtos, cdService.getOnlyCdByPlaylistId(playlistId));
    }


    @Transactional
    public String sharePlaylist(String userId) {
        Users users = usersRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        if (users.getShareCode() != null && !users.getShareCode().isBlank()) {
            return "/shared/" + users.getShareCode();
        }

        String shareCode = ShareCodeGenerator.generate(userId);
        users.assignShareCode(shareCode);
        usersRepository.save(users);

        return "/shared/" + shareCode;
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


    @Override
    @Transactional(readOnly = true)
    public FollowedPlaylistsResponse getFolloweePlaylists(String userId, PlaylistSortOption sort) {
        List<FollowedPlaylist> result = followRepository.findFolloweePlaylistsWithMeta(userId, sort, DEFAULT_LIMIT);
        return new FollowedPlaylistsResponse(result.size(), result);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlaylistDetailResponse> getPlaylistsByCreatorId(String creatorId) {
        List<Playlist> playlists = playlistRepository.findByUserIdRecent(creatorId);
        if (playlists.isEmpty()) {
            return List.of();
        }

        List<PlaylistDetailResponse> responses = new ArrayList<>(playlists.size());
        for (Playlist playlist : playlists) {
            List<Song> songs = songRepository.findSongsByPlaylistId(playlist.getId());
            List<SongDto> songDtos = songs.stream().map(SongDto::from).toList();
            responses.add(
                    PlaylistDetailResponse.from(playlist, songDtos, cdService.getOnlyCdByPlaylistId(playlist.getId())));
        }
        return responses;
    }
}
