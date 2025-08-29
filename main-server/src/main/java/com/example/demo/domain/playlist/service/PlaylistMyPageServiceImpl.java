package com.example.demo.domain.playlist.service;

import com.example.common.error.code.PlaylistErrorCode;
import com.example.common.error.code.UserErrorCode;
import com.example.common.error.exception.PlaylistException;
import com.example.common.error.exception.UserException;
import com.example.demo.domain.cd.dto.request.CdItemRequest;
import com.example.demo.domain.cd.service.CdService;
import com.example.demo.domain.follow.dto.FollowPlaylistDto;
import com.example.demo.domain.follow.dto.FollowPlaylistsResponse;
import com.example.demo.domain.follow.repository.FollowRepository;
import com.example.demo.domain.playlist.dto.*;
import com.example.demo.domain.playlist.dto.SongDto;
import com.example.demo.domain.playlist.dto.playlistdto.PlaylistCreateRequest;
import com.example.demo.domain.playlist.dto.playlistdto.PlaylistDetailResponse;
import com.example.demo.domain.playlist.dto.playlistdto.PlaylistResponse;
import com.example.demo.domain.playlist.dto.playlistdto.PlaylistWithSongsResponse;
import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.playlist.repository.PlaylistRepository;
import com.example.demo.domain.playlist.util.ShareCodeGenerator;
import com.example.demo.domain.representative.entity.RepresentativePlaylist;
import com.example.demo.domain.representative.repository.RepresentativePlaylistRepository;
import com.example.demo.domain.song.dto.SongMapper;
import com.example.demo.domain.song.dto.SongResponseDto;
import com.example.demo.domain.song.dto.YouTubeVideoInfoDto;
import com.example.demo.domain.song.entity.Song;
import com.example.demo.domain.song.repository.SongRepository;
import com.example.demo.domain.user.entity.Users;
import com.example.demo.domain.user.repository.UsersRepository;
import java.util.Optional;
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

    private static final int DEFAULT_LIMIT = 20;
    private final CdService cdService;

    @Override
    @Transactional
    public PlaylistWithSongsResponse saveFinalPlaylistWithSongsAndCd(String usersId, PlaylistCreateRequest request,
                                                                     List<CdItemRequest> cdItemRequestList){

        PlaylistWithSongsResponse response = playlistSaveService.savePlaylistWithSongs(usersId, request);

        cdService.saveCdItemList(response.playlistId(), cdItemRequestList);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlaylistResponse> getMyPlaylistsSorted(String userId, PlaylistSortOption sortOption) {
        log.info("🔍 내 플레이리스트 조회 시작: userId={}, sortOption={}", userId, sortOption);

        List<Playlist> all = switch (sortOption) {
            case POPULAR -> playlistRepository.findByUserIdPopular(userId);
            case RECENT  -> playlistRepository.findByUserIdRecent(userId);
        };

        log.info("🎵 조회된 플레이리스트 개수: {}", all.size());

        if (all.isEmpty()) {
            log.info("⚠️ 유저의 플레이리스트가 존재하지 않음: userId={}", userId);
            return List.of();
        }

        var repOpt = representativePlaylistRepository.findByUser_Id(userId);

        if (repOpt.isEmpty()) {
            log.info("ℹ️ 대표 플레이리스트 없음: userId={}", userId);
            return all.stream()
                    .map(PlaylistResponse::from)
                    .toList();
        }

        Playlist rep = repOpt.get().getPlaylist();
        log.info("🏅 대표 플레이리스트 ID: {}", rep.getId());

        List<Playlist> rest = all.stream()
                .filter(p -> !p.getId().equals(rep.getId()))
                .toList();

        List<PlaylistResponse> result = new ArrayList<>(rest.size() + 1);
        result.add(PlaylistResponse.from(rep));
        result.addAll(rest.stream().map(PlaylistResponse::from).toList());

        log.info("✅ 최종 반환되는 플레이리스트 개수: {}", result.size());
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public PlaylistDetailResponse getPlaylistDetail(String userId, Long playlistId) {
        Playlist playlist = playlistRepository.findByIdAndUsers_Id(playlistId, userId)
                .orElseThrow(() -> new PlaylistException("플레이리스트가 존재하지 않거나 권한이 없습니다.", PlaylistErrorCode.PLAYLIST_NOT_FOUND));

        List<Song> songs = songRepository.findSongsByPlaylistId(playlistId);
        List<SongDto> songDtos = songs.stream().map(SongDto::from).toList();

        return PlaylistDetailResponse.from(playlist, songDtos, cdService.getOnlyCdByPlaylistId(playlistId));
    }

    @Override
    @Transactional
    public void deletePlaylist(String userId, Long playlistId) {
        // 1. 삭제 대상 존재 및 권한 검증
        Playlist toDelete = playlistRepository.findByIdAndUsers_Id(playlistId, userId)
                .orElseThrow(() -> new PlaylistException(
                        "해당 플레이리스트가 존재하지 않거나 권한이 없습니다.",
                        PlaylistErrorCode.PLAYLIST_NOT_FOUND
                ));

        // 2. 유저가 가진 플레이리스트가 1개뿐이라면 삭제 불가
        long totalCount = playlistRepository.countByUserIdNative(userId);
        if (totalCount <= 1) {
            throw new PlaylistException(
                    "플레이리스트는 최소 1개 이상 존재해야 합니다.",
                    PlaylistErrorCode.PLAYLIST_NOT_FOUND
            );
        }

        // 3. 이 Playlist가 대표인지 확인
        boolean isRepresentative = representativePlaylistRepository.isRepresentativePlaylist(userId, playlistId);

        // 4. 만약 대표라면 기존 RepresentativePlaylist 정보 제거
        if (isRepresentative) {
            representativePlaylistRepository.deleteByUser_Id(userId);
            representativePlaylistRepository.deleteByPlaylist_Id(playlistId);
        }

        // 5. 실제 Playlist 및 곡 삭제
        songRepository.deleteByPlaylistId(playlistId);
        playlistRepository.delete(toDelete);

        // 6. 대표였던 경우 → 삭제 이후 가장 최근 플레이리스트를 대표로 지정
        if (isRepresentative) {
            playlistRepository.findMostRecentExcluding(userId, playlistId)
                    .ifPresent(newRepPlaylist -> {
                        // Playlist에 대표 표시
                        newRepPlaylist.changeToRepresentative();
                        playlistRepository.save(newRepPlaylist);

                        // RepresentativePlaylist 등록
                        Users user = toDelete.getUsers();
                        RepresentativePlaylist rep = new RepresentativePlaylist(user, newRepPlaylist);
                        representativePlaylistRepository.save(rep);
                    });
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
                .orElseThrow(() -> new PlaylistException("해당 플레이리스트가 존재하지 않거나 권한이 없습니다.", PlaylistErrorCode.PLAYLIST_NOT_FOUND));

        representativePlaylistRepository.findByUser_Id(userId)
                .ifPresentOrElse(
                        rep -> {
                            if (!rep.getPlaylist().getId().equals(target.getId())) {
                                rep.changePlaylist(target);
                            }
                        },
                        () -> representativePlaylistRepository.save(new RepresentativePlaylist(user, target))
                );
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
            responses.add(PlaylistDetailResponse.from(playlist, songDtos, cdService.getOnlyCdByPlaylistId(playlist.getId())));
        }
        return responses;
    }
}
