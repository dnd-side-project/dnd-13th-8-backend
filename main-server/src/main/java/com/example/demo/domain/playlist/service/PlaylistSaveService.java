package com.example.demo.domain.playlist.service;

import com.example.common.error.code.PlaylistErrorCode;
import com.example.common.error.code.UserErrorCode;
import com.example.common.error.exception.PlaylistException;
import com.example.common.error.exception.UserException;
import com.example.demo.domain.playlist.dto.PlaylistMapper;
import com.example.demo.domain.playlist.dto.playlistdto.PlaylistCreateRequest;
import com.example.demo.domain.playlist.dto.playlistdto.PlaylistWithSongsResponse;
import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.playlist.repository.PlaylistRepository;
import com.example.demo.domain.representative.entity.RepresentativePlaylist;
import com.example.demo.domain.representative.repository.RepresentativePlaylistRepository;
import com.example.demo.domain.song.dto.SongMapper;
import com.example.demo.domain.song.dto.SongResponseDto;
import com.example.demo.domain.song.dto.YouTubeVideoInfoDto;
import com.example.demo.domain.song.entity.Song;
import com.example.demo.domain.song.repository.SongRepository;
import com.example.demo.domain.user.entity.Users;
import com.example.demo.domain.user.repository.UsersRepository;

import java.util.*;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlaylistSaveService {

    private final PlaylistRepository playlistRepository;
    private final SongRepository songRepository;
    private final RepresentativePlaylistRepository representativePlaylistRepository;
    private final UsersRepository usersRepository;

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

    @Transactional
    public Playlist savePlaylist(String usersId, PlaylistCreateRequest request) {

        Users users = usersRepository.findById(usersId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        boolean isFirst = playlistRepository.countByUserIdNative(usersId) == 0;

        Playlist playlist = PlaylistMapper.toEntity(request, users);
        Playlist saved = playlistRepository.save(playlist);


        if (isFirst || request.isRepresentative()) {
            replaceRepresentativePlaylist(users, saved);
        }

        return saved;
    }

    @Transactional
    public PlaylistWithSongsResponse editPlaylistWithSongs(String usersId, Long playlistId,
                                                           PlaylistCreateRequest request) {
        Users users = usersRepository.findById(usersId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new PlaylistException(PlaylistErrorCode.PLAYLIST_NOT_FOUND));

        playlist.editPlaylist(request.name(),request.genre(), request.isRepresentative());

        playlistRepository.save(playlist); // playlist부터 수정

        List<Song> existingSongs = songRepository.findSongsByPlaylistId(playlistId);

        // 요청 링크 세트(검증된 링크라고 가정)
        Set<String> requestedLinks = request.youTubeVideoInfo().stream()
                .map(YouTubeVideoInfoDto::link)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        // 기존 링크 세트
        Set<String> existingLinks = existingSongs.stream()
                .map(Song::getYoutubeUrl)
                .collect(Collectors.toSet());

        // 1) 삭제: 기존엔 있으나 요청엔 없는 링크 -> id 수집 후 deleteAllByIdIn
        List<Long> deleteIds = existingSongs.stream()
                .filter(s -> !requestedLinks.contains(s.getYoutubeUrl()))
                .map(Song::getId)
                .toList();

        if (!deleteIds.isEmpty()) {
            songRepository.deleteAllByIdIn(deleteIds);
        }

        // 2) 추가: 요청엔 있으나 기존엔 없는 링크만 저장
        List<Song> toAdd = request.youTubeVideoInfo().stream()
                .filter(dto -> !existingLinks.contains(dto.link()))
                .map(dto -> SongMapper.toEntity(dto, playlist))
                .toList();

        if (!toAdd.isEmpty()) {
            songRepository.saveAll(toAdd);
        }

        // 대표 여부 처리
        if (request.isRepresentative()) {
            replaceRepresentativePlaylist(users, playlist);
        }

        // 응답
        List<SongResponseDto> songDtos = songRepository.findSongsByPlaylistId(playlistId).stream()
                .map(SongMapper::toDto)
                .toList();

        return new PlaylistWithSongsResponse(playlist.getId(), songDtos);
    }

    /**
     * 기존 대표 해제 → 새 대표 설정 → RepresentativePlaylist 갱신
     */
    public void replaceRepresentativePlaylist(Users user, Playlist newRepPlaylist) {
        String userId = user.getId();

        // 1. 기존 대표 isRepresentative false 처리
        playlistRepository.clearRepresentativeByUserId(userId);

        // 2. 새 대표 true 설정
        if (!newRepPlaylist.isRepresentative()) {
            newRepPlaylist.changeToRepresentative();
            playlistRepository.save(newRepPlaylist); // 안전하게 저장
        }

        // 3. RepresentativePlaylist 갱신
        representativePlaylistRepository.findByUser_Id(userId)
                .ifPresentOrElse(
                        rep -> rep.changePlaylist(newRepPlaylist),
                        () -> {
                            RepresentativePlaylist rep = new RepresentativePlaylist(user, newRepPlaylist);
                            representativePlaylistRepository.save(rep);
                        }
                );
    }
}
