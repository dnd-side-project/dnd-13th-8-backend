package com.example.demo.domain.playlist.controller;

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
import com.example.demo.domain.playlist.service.PlaylistService;
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

        List<Song> songs = songRepository.findByPlaylistId(playlistId);

        List<SongDto> songDtos = songs.stream()
                .map(SongDto::from)
                .toList();

        return PlaylistDetailResponse.from(playlist, songDtos);
    }


}
