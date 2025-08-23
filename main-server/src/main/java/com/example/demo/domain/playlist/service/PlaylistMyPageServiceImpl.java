package com.example.demo.domain.playlist.service;

import com.example.common.error.code.UserErrorCode;
import com.example.common.error.exception.UserException;
import com.example.demo.domain.playlist.dto.*;
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

    private static final int DEFAULT_LIMIT = 20;

    public Playlist savePlaylist(String usersId, PlaylistCreateRequest request) {
        Users users = usersRepository.findById(usersId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        boolean isFirst = playlistRepository.countByUsers_Id(usersId) == 0;

        Playlist playlist = PlaylistMapper.toEntity(request, users);
        Playlist saved = playlistRepository.save(playlist);


        if (isFirst || request.isRepresentative()) {
            updateRepresentativePlaylist(users, saved);
        }

        return saved;
    }

    private void updateRepresentativePlaylist(Users user, Playlist playlist) {
        representativePlaylistRepository.findByUser_Id(user.getId())
                .ifPresentOrElse(
                        r -> {
                            r.getPlaylist().setIsRepresentative(false);
                            r.changePlaylist(playlist);
                            playlist.setIsRepresentative(true);
                        },
                        () -> {
                            representativePlaylistRepository.save(new RepresentativePlaylist(user, playlist));
                            playlist.setIsRepresentative(true);
                        }
                );
    }



    @Override
    @Transactional
    public PlaylistWithSongsResponse savePlaylistWithSongs(String usersId, PlaylistCreateRequest request, String theme) {
        Playlist savedPlaylist = savePlaylist(usersId, request);

        List<Song> songsToSave = new ArrayList<>();
        for (YouTubeVideoInfoDto dto : request.songs()) {
            songsToSave.add(SongMapper.toEntity(dto, savedPlaylist));
        }

        List<Song> savedSongs = songRepository.saveAll(songsToSave);

        List<SongResponseDto> songDtos = savedSongs.stream()
                .map(SongMapper::toDto)
                .toList();

        return new PlaylistWithSongsResponse(savedPlaylist.getId(), songDtos);
    }

    @Override
    @Transactional
    public List<PlaylistResponse> getMyPlaylistsSorted(String userId, PlaylistSortOption sortOption) {
        RepresentativePlaylist representative = representativePlaylistRepository.findByUser_Id(userId)
                .orElseThrow(() -> new IllegalStateException("대표 플레이리스트가 존재하지 않습니다."));

        Playlist rep = representative.getPlaylist();

        List<Playlist> rest = switch (sortOption) {
            case POPULAR -> playlistRepository.findByUserIdPopular(userId);
            case RECENT -> playlistRepository.findByUserIdRecent(userId);
        };

        List<PlaylistResponse> result = new ArrayList<>();
        result.add(PlaylistResponse.from(rep));
        result.addAll(rest.stream().map(PlaylistResponse::from).toList());

        return result;
    }

    @Override
    public PlaylistDetailResponse getPlaylistDetail(String userId, Long playlistId) {
        Playlist playlist = playlistRepository.findByIdAndUsers_Id(playlistId, userId)
                .orElseThrow(() -> new IllegalStateException("플레이리스트가 존재하지 않습니다."));

        List<Song> songs = songRepository.findSongsByPlaylistId(playlistId);

        List<SongDto> songDtos = songs.stream()
                .map(SongDto::from)
                .toList();

        return PlaylistDetailResponse.from(playlist, songDtos);
    }

    @Override
    public void deletePlaylist(String userId, Long playlistId) {
        Playlist playlist = playlistRepository.findByIdAndUsers_Id(playlistId, userId)
                .orElseThrow(() -> new IllegalStateException("해당 플레이리스트가 존재하지 않거나 권한이 없습니다."));

        songRepository.deleteByPlaylistId(playlistId);
        playlistRepository.delete(playlist);
    }

    @Transactional
    public String sharePlaylist(String userId) {
        boolean exists = representativePlaylistRepository.existsByUser_Id(userId);
        if (!exists) {
            throw new IllegalStateException("대표 플레이리스트가 설정되어 있지 않습니다.");
        }
        return "/shared/" + userId;
    }


    @Override
    @Transactional
    public void updateRepresentative(String userId, Long playlistId) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        Playlist playlist = playlistRepository.findByIdAndUsers_Id(playlistId, userId)
                .orElseThrow(() -> new IllegalStateException("해당 플레이리스트가 존재하지 않거나 권한이 없습니다."));

        representativePlaylistRepository.findByUser_Id(userId)
                .ifPresentOrElse(
                        r -> r.changePlaylist(playlist),
                        () -> representativePlaylistRepository.save(new RepresentativePlaylist(user, playlist))
                );
    }

    @Override
    @Transactional(readOnly = true)
    public LikedPlaylistsResponse getLikedPlaylists(String userId, PlaylistSortOption sort) {
        List<LikedPlaylistDto> result = playlistRepository.findLikedPlaylistsWithMeta(userId, sort, DEFAULT_LIMIT);
        return new LikedPlaylistsResponse(result.size(), result);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlaylistDetailResponse> getPlaylistsByCreatorId(String creatorId) {
        RepresentativePlaylist representative = representativePlaylistRepository.findByUser_Id(creatorId)
                .orElseThrow(() -> new IllegalStateException("해당 유저의 대표 플레이리스트가 존재하지 않습니다."));

        Playlist playlist = representative.getPlaylist();
        List<Song> songs = songRepository.findSongsByPlaylistId(playlist.getId());
        List<SongDto> songDtos = songs.stream().map(SongDto::from).toList();

        return List.of(PlaylistDetailResponse.from(playlist, songDtos));
    }
}
