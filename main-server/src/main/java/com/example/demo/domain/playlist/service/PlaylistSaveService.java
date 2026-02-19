package com.example.demo.domain.playlist.service;

import com.example.common.error.code.PlaylistErrorCode;
import com.example.common.error.code.UserErrorCode;
import com.example.common.error.exception.PlaylistException;
import com.example.common.error.exception.UserException;
import com.example.demo.domain.cd.dto.request.SaveCdRequest;
import com.example.demo.domain.playlist.dto.save.PlaylistDraft;
import com.example.demo.domain.playlist.dto.save.PlaylistMapper;
import com.example.demo.domain.playlist.dto.save.SavePlaylistRequest;
import com.example.demo.domain.playlist.dto.save.SavePlaylistResponse;
import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.playlist.repository.PlaylistRepository;
import com.example.demo.domain.song.dto.SongMapper;
import com.example.demo.domain.song.dto.SongResponseDto;
import com.example.demo.domain.song.dto.YouTubeVideoInfoDto;
import com.example.demo.domain.song.entity.Song;
import com.example.demo.domain.song.repository.SongRepository;
import com.example.demo.domain.user.entity.Users;
import com.example.demo.domain.user.repository.UsersRepository;

import java.time.Duration;
import java.util.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlaylistSaveService {

    private final PlaylistRepository playlistRepository;
    private final SongRepository songRepository;
    private final UsersRepository usersRepository;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    private static final String PREFIX = "playlist:draft:";

    public String createDraft(SavePlaylistRequest savePlaylistRequest,
                                      SaveCdRequest saveCdRequest) {
        String draftId = UUID.randomUUID().toString();
        PlaylistDraft draft = new PlaylistDraft(
                savePlaylistRequest,
                saveCdRequest
        );
        saveDraft(draftId, draft);
        return draftId;
    }

    public PlaylistDraft loadDraft(String draftId) {
        String key = PREFIX + draftId;
        String json = stringRedisTemplate.opsForValue().get(key);

        log.info("loadDraft key={}, json={}", key, json);

        if (json == null) throw new PlaylistException(PlaylistErrorCode.PLAYLIST_DRAFT_ERROR);

        try {
            return objectMapper.readValue(json, PlaylistDraft.class);
        } catch (JsonProcessingException e) {
            log.warn("parse fail draftId={}, json={}", draftId, json, e);
            throw new PlaylistException(PlaylistErrorCode.PLAYLIST_DRAFT_ERROR);
        }
    }

    private void saveDraft(String draftId, PlaylistDraft draft) {
        try {
            String json = objectMapper.writeValueAsString(draft);
            stringRedisTemplate.opsForValue().set(PREFIX + draftId, json, Duration.ofHours(1));
        } catch (JsonProcessingException e) {
            throw new PlaylistException(PlaylistErrorCode.PLAYLIST_DRAFT_ERROR);
        }
    }

    public void deleteDraft(String draftId) {
        stringRedisTemplate.delete(PREFIX + draftId);
    }

    @Transactional
    public SavePlaylistResponse savePlaylistWithSongs(String usersId, SavePlaylistRequest request) {

        Playlist savedPlaylist = savePlaylist(usersId, request);

        List<Song> songsToSave = new ArrayList<>();
        for (YouTubeVideoInfoDto dto : request.youTubeVideoInfo()) {
            songsToSave.add(SongMapper.toEntity(dto, savedPlaylist));
        }

        List<Song> savedSongs = songRepository.saveAll(songsToSave);

        List<SongResponseDto> songDtos = savedSongs.stream()
                .map(SongMapper::toDto)
                .toList();

        return new SavePlaylistResponse(savedPlaylist.getId(), songDtos);
    }

    @Transactional
    public Playlist savePlaylist(String usersId, SavePlaylistRequest request) {

        Users users = usersRepository.findById(usersId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        Playlist playlist = PlaylistMapper.toEntity(request, users);

        return playlistRepository.save(playlist);
    }

    @Transactional
    public SavePlaylistResponse editPlaylistWithSongs(
            String usersId, Long playlistId, SavePlaylistRequest request) {

        Users users = usersRepository.findById(usersId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new PlaylistException(PlaylistErrorCode.PLAYLIST_NOT_FOUND));

        playlist.editPlaylist(request.name(), request.genre(), request.isPublic());
        playlistRepository.save(playlist);

        // 1) 기존 곡 전부 삭제
        songRepository.deleteByPlaylistId(playlistId);

        // 2) 요청 순서(YouTubeVideoInfoDto.orderIndex)를 그대로 저장
        List<Song> toSave = request.youTubeVideoInfo().stream()
                .sorted(Comparator.comparing(YouTubeVideoInfoDto::orderIndex)) // 안전하게 정렬
                .map(dto -> SongMapper.toEntity(dto, playlist))
                .toList();

        if (!toSave.isEmpty()) {
            songRepository.saveAll(toSave);
        }

        // 3) 응답
        List<SongResponseDto> songDtos = songRepository
                .findSongsByPlaylistId(playlistId)
                .stream()
                .map(SongMapper::toDto)
                .toList();

        return new SavePlaylistResponse(playlist.getId(), songDtos);
    }

}
