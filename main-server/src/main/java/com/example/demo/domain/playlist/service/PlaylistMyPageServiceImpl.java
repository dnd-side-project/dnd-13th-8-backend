package com.example.demo.domain.playlist.service;

import com.example.common.error.code.UserErrorCode;
import com.example.common.error.exception.UserException;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaylistMyPageServiceImpl implements PlaylistMyPageService {

    private final PlaylistRepository playlistRepository;
    private final SongRepository songRepository;
    private final UsersRepository usersRepository;
    private final RepresentativePlaylistRepository representativePlaylistRepository;
    private final FollowRepository followRepository;

    private static final int DEFAULT_LIMIT = 20;

    /**
     * 플레이리스트 저장 (신규/대표 지정 요청 시 대표 매핑 처리)
     */
    @Transactional
    public Playlist savePlaylist(String usersId, PlaylistCreateRequest request) {
        Users users = usersRepository.findById(usersId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        boolean isFirst = playlistRepository.countByUsers_Id(usersId) == 0;

        Playlist playlist = PlaylistMapper.toEntity(request, users);
        Playlist saved = playlistRepository.save(playlist);

        if (isFirst || request.isRepresentative()) {
            upsertRepresentative(users, saved);
        }

        return saved;
    }

    /**
     * 사용자 대표 플리 매핑 생성/갱신
     * - 기존 대표가 있으면 새 플리로 교체
     * - 없으면 생성
     * - Playlist.isRepresentative 플래그는 사용하지 않음(대표 여부는 RepresentativePlaylist로만 판단)
     */
    private void upsertRepresentative(Users user, Playlist target) {
        representativePlaylistRepository.findByUser_Id(user.getId())
                .ifPresentOrElse(
                        rep -> {
                            if (!rep.getPlaylist().getId().equals(target.getId())) {
                                rep.changePlaylist(target);
                            }
                        },
                        () -> representativePlaylistRepository.save(new RepresentativePlaylist(user, target))
                );
    }

    /**
     * 플레이리스트 + 곡 함께 저장
     */
    @Override
    @Transactional
    public PlaylistWithSongsResponse savePlaylistWithSongs(String usersId, PlaylistCreateRequest request) {
        Playlist savedPlaylist = savePlaylist(usersId, request);

        List<Song> songsToSave = new ArrayList<>();
        for (YouTubeVideoInfoDto dto : request.youTubeVideoInfo()) {
            songsToSave.add(SongMapper.toEntity(dto, savedPlaylist));
        }

        List<Song> savedSongs = songRepository.saveAll(songsToSave);
        List<SongResponseDto> songDtos = savedSongs.stream()
                .map(SongMapper::toDto)
                .toList();

        return new PlaylistWithSongsResponse(savedPlaylist.getId(), songDtos);
    }

    /**
     * 마이페이지: 내 모든 플레이리스트 조회 (정렬 기준 반영, 대표가 있으면 맨 앞)
     * - 대표 플리가 없어도 예외 없이 동작
     */
    @Override
    @Transactional(readOnly = true)
    public List<PlaylistResponse> getMyPlaylistsSorted(String userId, PlaylistSortOption sortOption) {
        List<Playlist> all = switch (sortOption) {
            case POPULAR -> playlistRepository.findByUserIdPopular(userId);
            case RECENT  -> playlistRepository.findByUserIdRecent(userId);
        };

        if (all.isEmpty()) {
            return List.of();
        }

        var repOpt = representativePlaylistRepository.findByUser_Id(userId);
        if (repOpt.isEmpty()) {
            return all.stream().map(PlaylistResponse::from).toList();
        }

        Playlist rep = repOpt.get().getPlaylist();

        // 중복 방지: 전체 목록에서 대표 제거
        List<Playlist> rest = all.stream()
                .filter(p -> !p.getId().equals(rep.getId()))
                .toList();

        List<PlaylistResponse> result = new ArrayList<>(rest.size() + 1);
        result.add(PlaylistResponse.from(rep)); // 대표 먼저
        result.addAll(rest.stream().map(PlaylistResponse::from).toList());
        return result;
    }

    /**
     * 내 특정 플레이리스트 상세
     */
    @Override
    @Transactional(readOnly = true)
    public PlaylistDetailResponse getPlaylistDetail(String userId, Long playlistId) {
        Playlist playlist = playlistRepository.findByIdAndUsers_Id(playlistId, userId)
                .orElseThrow(() -> new IllegalStateException("플레이리스트가 존재하지 않습니다."));

        List<Song> songs = songRepository.findSongsByPlaylistId(playlistId);
        List<SongDto> songDtos = songs.stream().map(SongDto::from).toList();

        return PlaylistDetailResponse.from(playlist, songDtos);
    }

    /**
     * 내 플레이리스트 삭제
     * - 삭제 대상이 대표라면: 남아있는 플리 중 최신 1개를 대표로 자동 재지정
     * - 남은 게 없으면 대표 해제
     */
    @Override
    @Transactional
    public void deletePlaylist(String userId, Long playlistId) {
        Playlist toDelete = playlistRepository.findByIdAndUsers_Id(playlistId, userId)
                .orElseThrow(() -> new IllegalStateException("해당 플레이리스트가 존재하지 않거나 권한이 없습니다."));

        var repOpt = representativePlaylistRepository.findByUser_Id(userId);

        // 곡 삭제 → 플리 삭제 (FK 제약 및 on delete 정책에 따라 순서 유지)
        songRepository.deleteByPlaylistId(playlistId);
        playlistRepository.delete(toDelete);

        // 대표가 삭제된 경우 처리
        if (repOpt.isPresent()) {
            RepresentativePlaylist rep = repOpt.get();
            if (rep.getPlaylist().getId().equals(playlistId)) {
                // 최신 1개 선택(삭제한 것 제외). 없으면 대표 해제
                playlistRepository.findNextRecent(userId, playlistId)
                        .ifPresentOrElse(
                                rep::changePlaylist,
                                () -> representativePlaylistRepository.delete(rep)
                        );
            }
        }
    }

    /**
     * 공유 링크 발급
     */
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

    /**
     * 대표 플리 변경 (사용자당 1개 보장: 기존 대표는 자동 해제/교체)
     */
    @Override
    @Transactional
    public void updateRepresentative(String userId, Long playlistId) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        Playlist target = playlistRepository.findByIdAndUsers_Id(playlistId, userId)
                .orElseThrow(() -> new IllegalStateException("해당 플레이리스트가 존재하지 않거나 권한이 없습니다."));

        representativePlaylistRepository.findByUser_Id(userId)
                .ifPresentOrElse(
                        rep -> {
                            if (!rep.getPlaylist().getId().equals(target.getId())) {
                                rep.changePlaylist(target); // 기존 대표 해제 + 새 대표 지정
                            }
                        },
                        () -> representativePlaylistRepository.save(new RepresentativePlaylist(user, target))
                );
    }

    /**
     * 내가 팔로우한 사람들의 대표/플리 메타 조회
     */
    @Override
    @Transactional(readOnly = true)
    public FollowPlaylistsResponse getFolloweePlaylists(String userId, PlaylistSortOption sort) {
        List<FollowPlaylistDto> result = followRepository.findFolloweePlaylistsWithMeta(userId, sort, DEFAULT_LIMIT);
        return new FollowPlaylistsResponse(result.size(), result);
    }

    /**
     * 특정 크리에이터의 모든 플레이리스트 조회 (대표 강제 X, 기본 최신순)
     */
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
            responses.add(PlaylistDetailResponse.from(playlist, songDtos));
        }
        return responses;
    }
}
