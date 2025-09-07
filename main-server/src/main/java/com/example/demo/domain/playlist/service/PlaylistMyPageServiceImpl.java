package com.example.demo.domain.playlist.service;

import com.example.common.error.code.PlaylistErrorCode;
import com.example.common.error.code.UserErrorCode;
import com.example.common.error.exception.PlaylistException;
import com.example.common.error.exception.UserException;
import com.example.demo.domain.cd.repository.CdRepository;
import com.example.demo.domain.cd.service.CdService;
import com.example.demo.domain.follow.dto.FollowPlaylistDto;
import com.example.demo.domain.follow.dto.FollowPlaylistsResponse;
import com.example.demo.domain.follow.repository.FollowRepository;
import com.example.demo.domain.playlist.dto.*;
import com.example.demo.domain.playlist.dto.SongDto;
import com.example.demo.domain.playlist.dto.playlistdto.PlaylistDetailResponse;
import com.example.demo.domain.playlist.dto.playlistdto.PlaylistResponse;
import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.playlist.repository.PlaylistRepository;
import com.example.demo.domain.playlist.util.ShareCodeGenerator;
import com.example.demo.domain.recommendation.repository.UserPlaylistHistoryRepository;
import com.example.demo.domain.representative.repository.RepresentativePlaylistRepository;
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
    private final RepresentativePlaylistRepository representativePlaylistRepository;
    private final FollowRepository followRepository;
    private final PlaylistSaveService playlistSaveService;
    private final PlaylistDeleteService playlistDeleteService;
    private final CdRepository cdRepository;
    private final FollowRepository userFollowPlaylistRepository; // 팔로우
    private final UserPlaylistHistoryRepository userPlaylistHistoryRepository;


    private static final int DEFAULT_LIMIT = 20;
    private final CdService cdService;

    @Override
    @Transactional(readOnly = true)
    public List<PlaylistResponse> getMyPlaylistsSorted(String userId, PlaylistSortOption sortOption) {
        List<Playlist> all = switch (sortOption) {
            case POPULAR -> playlistRepository.findByUserIdPopular(userId);
            case RECENT -> playlistRepository.findByUserIdRecent(userId);
        };

        if (all.isEmpty()) {
            return List.of();
        }

        var repOpt = representativePlaylistRepository.findByUser_Id(userId);

        if (repOpt.isEmpty()) {
            return all.stream()
                    .map(p -> PlaylistResponse.from(p, cdService.getOnlyCdByPlaylistId(p.getId())))
                    .toList();
        }

        Playlist rep = repOpt.get().getPlaylist();

        List<Playlist> rest = all.stream()
                .filter(p -> !p.getId().equals(rep.getId()))
                .toList();

        List<PlaylistResponse> result = new ArrayList<>(rest.size() + 1);
        result.add(PlaylistResponse.from(rep, cdService.getOnlyCdByPlaylistId(rep.getId())));
        result.addAll(rest.stream()
                .map(p -> PlaylistResponse.from(p, cdService.getOnlyCdByPlaylistId(p.getId())))
                .toList());

        return result;
    }


    @Override
    @Transactional(readOnly = true)
    public PlaylistDetailResponse getPlaylistDetail(String userId, Long playlistId) {
        Playlist playlist = playlistRepository.findByIdAndUsers_Id(playlistId, userId)
                .orElseThrow(() -> new PlaylistException("플레이리스트가 존재하지 않거나 권한이 없습니다.",
                        PlaylistErrorCode.PLAYLIST_NOT_FOUND));

        List<Song> songs = songRepository.findSongsByPlaylistId(playlistId);
        List<SongDto> songDtos = songs.stream().map(SongDto::from).toList();

        return PlaylistDetailResponse.from(playlist, songDtos, cdService.getOnlyCdByPlaylistId(playlistId));
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

        // 3. 대표 여부 확인
        boolean isRepresentative = representativePlaylistRepository.isRepresentativePlaylist(userId, playlistId);
        if (isRepresentative) {
            representativePlaylistRepository.deleteByPlaylist_Id(playlistId);
        }

        //  4. 참조 테이블 순차 삭제 (중요!)
        cdRepository.deleteByPlaylistId(playlistId); // CD 테이블
        songRepository.deleteByPlaylistId(playlistId); // 곡
        userFollowPlaylistRepository.deleteByPlaylistId(playlistId); // 팔로우
        userPlaylistHistoryRepository.deleteByPlaylistId(playlistId); // 재생기록

        // 5. 플레이리스트 삭제
        playlistRepository.delete(toDelete);

        // 6. 대표였던 경우 → 새 대표 설정
        if (isRepresentative) {
            playlistDeleteService.assignNewRepresentativeIfNecessary(userId, toDelete.getId());
        }
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
    public void updateRepresentative(String userId, Long playlistId) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        Playlist target = playlistRepository.findByIdAndUsers_Id(playlistId, userId)
                .orElseThrow(() ->new PlaylistException(
                            "해당 플레이리스트가 존재하지 않거나 권한이 없습니다.",
                            PlaylistErrorCode.PLAYLIST_NOT_FOUND));
        playlistSaveService.replaceRepresentativePlaylist(user, target);
    }


    @Override
    @Transactional(readOnly = true)
    public FollowPlaylistsResponse getFolloweePlaylists(String userId, PlaylistSortOption sort) {
        List<FollowPlaylistDto> result = followRepository.findFolloweePlaylistsWithMeta(userId, sort, DEFAULT_LIMIT);
        return new FollowPlaylistsResponse(result.size(), result);
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
