package com.example.demo.domain.playlist.service;

import com.example.common.error.code.UserErrorCode;
import com.example.common.error.exception.UserException;
import com.example.demo.domain.playlist.dto.PlaylistCreateRequest;
import com.example.demo.domain.playlist.dto.PlaylistDetailResponse;
import com.example.demo.domain.playlist.dto.PlaylistMapper;
import com.example.demo.domain.playlist.dto.PlaylistResponse;
import com.example.demo.domain.playlist.dto.PlaylistSortOption;
import com.example.demo.domain.playlist.dto.PlaylistWithSongsResponse;
import com.example.demo.domain.playlist.dto.SongDto;
import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.playlist.repository.PlaylistRepository;
import com.example.demo.domain.playlist.util.ShareCodeGenerator;
import com.example.demo.domain.song.dto.SongResponseDto;
import com.example.demo.domain.song.entity.Song;
import com.example.demo.domain.song.repository.SongRepository;
import com.example.demo.domain.song.service.YouTubeSongService;
import com.example.demo.domain.user.entity.Users;
import com.example.demo.domain.user.repository.UsersRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public  class PlaylistServiceImpl implements PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final YouTubeSongService songService;
    private final SongRepository songRepository;
    private final UsersRepository usersRepository;

    @Transactional
    public Playlist saveBlockingPlaylist(String usersId, PlaylistCreateRequest request, String theme) {

        Users users = usersRepository.findById(usersId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        boolean isFirst = playlistRepository.countByUsers_Id(usersId) == 0;
        boolean shouldBeRepresentative;

        if (isFirst) {
            shouldBeRepresentative = true; // 첫 플레이리스트면 무조건 대표
        } else if (request.isRepresentative()) {
            shouldBeRepresentative = true; // 사용자가 대표로 지정했으면 대표
        } else {
            shouldBeRepresentative = false; // 아니면 대표 아님
        }

        if (shouldBeRepresentative) {
            playlistRepository.clearPreviousRepresentative(usersId);
        }



        Playlist playlist = PlaylistMapper.toEntity(
                request,
                theme,
                users,
                shouldBeRepresentative
        );

        return playlistRepository.save(playlist);
    }

    @Override
    public PlaylistWithSongsResponse savePlaylistWithSongs(String users, PlaylistCreateRequest request, String theme) {
        // 1. blocking 저장 (JPA)
        Playlist savedPlaylist = saveBlockingPlaylist(users, request, theme);

        // 2. 비동기 저장 (R2DBC)
        List<SongResponseDto> savedSongs = songService.saveReactiveSongs(request.songs(), savedPlaylist.getId())
                .block();// <- 혼합 시점에서 불가피하게 block 사용

        return new PlaylistWithSongsResponse(savedPlaylist.getId(), savedSongs);
    }

    @Transactional
    @Override
    public List<PlaylistResponse> getMyPlaylistsSorted(String userId, PlaylistSortOption sortOption) {

        Playlist representative = playlistRepository.findRepresentativeByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("대표 플레이리스트가 존재하지 않습니다."));

        List<Playlist> rest = switch (sortOption) {
            case POPULAR -> playlistRepository.findByUserIdPopular(userId);
            case RECENT -> playlistRepository.findByUserIdRecent(userId);
        };

        List<PlaylistResponse> result = new ArrayList<>();
        result.add(PlaylistResponse.from(representative));
        result.addAll(rest.stream()
                .map(PlaylistResponse::from)
                .toList());

        return result;
    }

    @Override
    public PlaylistDetailResponse getPlaylistDetail(String userId, Long playlistId) {
        Playlist playlist = playlistRepository.findByIdAndUsers_Id(playlistId, userId).
                orElseThrow(() -> new IllegalStateException("대표 플레이리스트가 존재하지 않습니다."));

        List<Song> songs = songRepository.findAllByPlaylistId(playlistId)
                .collectList()
                .block(); // 비동기 Flux → 동기 List<Song> 변환


        List<SongDto> songDtos = songs.stream()
                .map(SongDto::from)
                .toList();

        return PlaylistDetailResponse.from(playlist, songDtos);
    }

    @Override
    public void deletePlaylist(String userId, Long playlistId) {
        Playlist playlist = playlistRepository.findByIdAndUsers_Id(playlistId, userId)
                .orElseThrow(() -> new IllegalStateException("해당 플레이리스트가 존재하지 않거나 권한이 없습니다."));

        // 연관된 Song 먼저 삭제 (R2DBC)
        songRepository.deleteAllByPlaylistId(playlistId).then().block();

        // 플레이리스트 삭제
        playlistRepository.delete(playlist);
    }

    @Transactional
    public String sharePlaylist(String userId, Long playlistId) {
        Playlist playlist = playlistRepository.findByIdAndUsers_Id(playlistId, userId)
                .orElseThrow(() -> new IllegalStateException("해당 플레이리스트가 존재하지 않거나 권한이 없습니다."));

        //  대표 플레이리스트가 아닌 경우 예외 처리
        if (!playlist.getIsRepresentative()) {
            throw new IllegalStateException("대표 플레이리스트만 공유할 수 있습니다.");
        }

        // 이미 공유된 경우 기존 코드 반환
        if (playlist.getIsShared()) {
            return playlist.getShareCode();
        }

        // UUID 기반 8자리 공유코드 생성 (중복 방지)
        String shareCode;
        do {
            shareCode = ShareCodeGenerator.generate(); // e.g., "a3f9b8c1"
        } while (playlistRepository.existsByShareCode(shareCode));

        playlist.startShare(shareCode);

        return shareCode;
    }

}
