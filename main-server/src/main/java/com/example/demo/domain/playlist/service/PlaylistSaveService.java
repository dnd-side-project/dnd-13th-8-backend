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

        // 기존 곡
        List<Song> existingSongs = songRepository.findSongsByPlaylistId(playlistId);

        // 새로 요청된 곡
        List<String> requestedLinks = request.youTubeVideoInfo().stream()
                .map(YouTubeVideoInfoDto::link)
                .map(String::trim)
                .toList();

        Map<String, Long> existingCount = existingSongs.stream()
                .collect(Collectors.groupingBy(Song::getYoutubeUrl, Collectors.counting()));

        Map<String, Long> requestedCount = requestedLinks.stream()
                .collect(Collectors.groupingBy(link -> link, Collectors.counting()));

        // 삭제 로직
        List<Long> deleteIds = new ArrayList<>();
        for (Song song : existingSongs) {
            String url = song.getYoutubeUrl();
            long reqCnt = requestedCount.getOrDefault(url, 0L);
            long existCnt = existingCount.getOrDefault(url, 0L);

            if (existCnt > reqCnt) {
                // 삭제 필요 → 하나씩 줄여나감
                deleteIds.add(song.getId());
                existingCount.put(url, existCnt - 1); // 카운트 감소
            }
        }
        if (!deleteIds.isEmpty()) {
            songRepository.deleteAllByIdIn(deleteIds);
        }

        // 추가
        List<Song> toAdd = new ArrayList<>();
        for (Map.Entry<String, Long> entry : requestedCount.entrySet()) {
            String url = entry.getKey();
            long reqCnt = entry.getValue();

            long existCnt = existingSongs.stream()
                    .filter(s -> s.getYoutubeUrl().equals(url))
                    .count();

            if (reqCnt > existCnt) {
                long needToAdd = reqCnt - existCnt;
                request.youTubeVideoInfo().stream()
                        .filter(dto -> dto.link().equals(url))
                        .limit(needToAdd) // 필요한 만큼만 생성
                        .forEach(dto -> toAdd.add(SongMapper.toEntity(dto, playlist)));
            }
        }
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
