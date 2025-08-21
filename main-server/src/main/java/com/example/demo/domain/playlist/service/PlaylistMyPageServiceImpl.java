package com.example.demo.domain.playlist.service;

import com.example.common.error.code.UserErrorCode;
import com.example.common.error.exception.UserException;
import com.example.demo.domain.cd.repository.CdRepository;
import com.example.demo.domain.playlist.dto.*;
import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.playlist.repository.PlaylistRepository;
import com.example.demo.domain.playlist.util.ShareCodeGenerator;
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
    private static final int DEFAULT_LIMIT = 20;

    @Transactional
    public Playlist saveBlockingPlaylist(String usersId, PlaylistCreateRequest request, String theme) {
        Users users = usersRepository.findById(usersId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        boolean isFirst = playlistRepository.countByUsers_Id(usersId) == 0;
        boolean shouldBeRepresentative = isFirst || request.isRepresentative();

        if (shouldBeRepresentative) {
            playlistRepository.clearPreviousRepresentative(usersId);
        }

        Playlist playlist = PlaylistMapper.toEntity(request, theme, users, shouldBeRepresentative);
        return playlistRepository.save(playlist);
    }

    @Override
    public PlaylistWithSongsResponse savePlaylistWithSongs(String usersId, PlaylistCreateRequest request, String theme) {
        // 1. playlist 저장
        Playlist savedPlaylist = saveBlockingPlaylist(usersId, request, theme);

        // 2. YouTubeVideoInfoDto → Song 엔티티 변환 (연관관계 포함)
        List<Song> songsToSave = new ArrayList<>();
        for (YouTubeVideoInfoDto dto : request.songs()) {
            songsToSave.add(SongMapper.toEntity(dto, savedPlaylist));
        }

        // 3. 저장
        List<Song> savedSongs = songRepository.saveAll(songsToSave);

        // 4. 응답 DTO 변환
        List<SongResponseDto> songDtos = savedSongs.stream()
                .map(SongMapper::toDto)
                .toList();

        return new PlaylistWithSongsResponse(savedPlaylist.getId(), songDtos);
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
    public String sharePlaylist(String userId, Long playlistId) {
        Playlist playlist = playlistRepository.findByIdAndUsers_Id(playlistId, userId)
                .orElseThrow(() -> new IllegalStateException("해당 플레이리스트가 존재하지 않거나 권한이 없습니다."));

        if (!playlist.getIsRepresentative()) {
            throw new IllegalStateException("대표 플레이리스트만 공유할 수 있습니다.");
        }

        if (playlist.getIsShared()) {
            return playlist.getShareCode();
        }

        String shareCode;
        do {
            shareCode = ShareCodeGenerator.generate();
        } while (playlistRepository.existsByShareCode(shareCode));

        playlist.startShare(shareCode);
        return shareCode;
    }

    @Override
    @Transactional
    public void updateRepresentative(String userId, Long playlistId) {
        Playlist playlist = playlistRepository.findByIdAndUsers_Id(playlistId, userId)
                .orElseThrow(() -> new IllegalStateException("해당 플레이리스트가 존재하지 않거나 권한이 없습니다."));

        playlistRepository.clearPreviousRepresentative(userId);
        playlist.changeToRepresentative();
    }

    @Override
    @Transactional(readOnly = true)
    public LikedPlaylistsResponse getLikedPlaylists(String userId, PlaylistSortOption sort) {
        List<LikedPlaylistDto> result = playlistRepository.findLikedPlaylistsWithMeta(userId, sort, 20);
        return new LikedPlaylistsResponse(result.size(), result);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlaylistDetailResponse> getPlaylistsByCreatorId(String creatorId) {
        return playlistRepository.findPlaylistsWithSongsByCreatorId(creatorId);
    }
}
