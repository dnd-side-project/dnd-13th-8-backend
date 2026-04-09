package com.example.demo.domain.playlist.service.common;

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
import com.example.demo.domain.playlist.repository.command.PlaylistRepository;
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
public class PlaylistSaveServiceImpl implements PlaylistSaveService {

    private static final String PREFIX = "playlist:draft:";

    private final PlaylistRepository playlistRepository;
    private final SongRepository songRepository;
    private final UsersRepository usersRepository;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public String createDraft(SavePlaylistRequest savePlaylistRequest, SaveCdRequest saveCdRequest) {
        String draftId = UUID.randomUUID().toString();
        PlaylistDraft draft = new PlaylistDraft(savePlaylistRequest, saveCdRequest);
        persistDraft(draftId, draft);
        return draftId;
    }

    @Override
    public PlaylistDraft loadDraft(String draftId) {
        String key = draftKey(draftId);

        String json = Optional.ofNullable(stringRedisTemplate.opsForValue().get(key))
                .orElseThrow(() -> new PlaylistException(PlaylistErrorCode.PLAYLIST_DRAFT_ERROR));

        log.info("loadDraft key={}, json={}", key, json);

        return deserializeDraft(json, draftId);
    }

    @Override
    public void deleteDraft(String draftId) {
        stringRedisTemplate.delete(draftKey(draftId));
    }

    @Override
    @Transactional
    public SavePlaylistResponse savePlaylistWithSongs(String usersId, SavePlaylistRequest request) {
        Users user = getUser(usersId);
        Playlist savedPlaylist = savePlaylist(request, user);

        List<Song> savedSongs = saveSongs(toSongs(request, savedPlaylist));

        return toSavePlaylistResponse(savedPlaylist, savedSongs);
    }

    @Override
    @Transactional
    public SavePlaylistResponse editPlaylistWithSongs(
            String usersId,
            Long playlistId,
            SavePlaylistRequest request
    ) {
        validateUserExists(usersId);

        Playlist playlist = getOwnedPlaylist(playlistId, usersId);
        updatePlaylist(playlist, request);

        replaceSongs(playlistId, toSortedSongs(request, playlist));

        List<SongResponseDto> songDtos = findSongResponses(playlistId);

        return new SavePlaylistResponse(playlist.getId(), songDtos);
    }

    private Users getUser(String usersId) {
        return usersRepository.findById(usersId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    }

    private void validateUserExists(String usersId) {
        getUser(usersId);
    }

    private List<Song> toSongs(SavePlaylistRequest request, Playlist playlist) {
        return request.youTubeVideoInfo().stream()
                .map(dto -> SongMapper.toEntity(dto, playlist))
                .toList();
    }

    private List<Song> toSortedSongs(SavePlaylistRequest request, Playlist playlist) {
        return request.youTubeVideoInfo().stream()
                .sorted(Comparator.comparing(YouTubeVideoInfoDto::orderIndex))
                .map(dto -> SongMapper.toEntity(dto, playlist))
                .toList();
    }

    private List<SongResponseDto> toSongResponseDtos(List<Song> songs) {
        return songs.stream()
                .map(SongMapper::toDto)
                .toList();
    }

    private SavePlaylistResponse toSavePlaylistResponse(Playlist playlist, List<Song> songs) {
        return new SavePlaylistResponse(
                playlist.getId(),
                toSongResponseDtos(songs)
        );
    }

    private String draftKey(String draftId) {
        return PREFIX + draftId;
    }

    private String serializeDraft(PlaylistDraft draft) {
        try {
            return objectMapper.writeValueAsString(draft);
        } catch (JsonProcessingException e) {
            throw new PlaylistException(PlaylistErrorCode.PLAYLIST_DRAFT_ERROR);
        }
    }

    private PlaylistDraft deserializeDraft(String json, String draftId) {
        try {
            return objectMapper.readValue(json, PlaylistDraft.class);
        } catch (JsonProcessingException e) {
            log.warn("parse fail draftId={}, json={}", draftId, json, e);
            throw new PlaylistException(PlaylistErrorCode.PLAYLIST_DRAFT_ERROR);
        }
    }

    private void persistDraft(String draftId, PlaylistDraft draft) {
        stringRedisTemplate.opsForValue().set(
                draftKey(draftId),
                serializeDraft(draft),
                Duration.ofHours(1)
        );
    }

    private Playlist savePlaylist(SavePlaylistRequest request, Users user) {
        Playlist playlist = PlaylistMapper.toEntity(request, user);
        return playlistRepository.save(playlist);
    }

    private List<Song> saveSongs(List<Song> songs) {
        return songs.isEmpty() ? List.of() : songRepository.saveAll(songs);
    }

    private void updatePlaylist(Playlist playlist, SavePlaylistRequest request) {
        playlist.editPlaylist(request.name(), request.genre(), request.isPublic());
    }

    private void replaceSongs(Long playlistId, List<Song> songs) {
        songRepository.deleteByPlaylistId(playlistId);
        if (!songs.isEmpty()) {
            songRepository.saveAll(songs);
        }
    }

    private List<SongResponseDto> findSongResponses(Long playlistId) {
        return songRepository.findSongsByPlaylistId(playlistId).stream()
                .map(SongMapper::toDto)
                .toList();
    }

    private Playlist getOwnedPlaylist(Long playlistId, String userId) {
        return playlistRepository.findByIdAndUsers_Id(playlistId, userId)
                .orElseThrow(() -> new PlaylistException(
                        "해당 플레이리스트가 존재하지 않거나 권한이 없습니다.",
                        PlaylistErrorCode.PLAYLIST_NOT_FOUND
                ));
    }

}
