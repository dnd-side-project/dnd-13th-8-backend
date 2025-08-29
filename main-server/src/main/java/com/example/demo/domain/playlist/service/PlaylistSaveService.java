package com.example.demo.domain.playlist.service;

import com.example.common.error.code.UserErrorCode;
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
import java.util.ArrayList;
import java.util.List;
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

    /**
     * 기존 대표 해제 → 새 대표 설정 → RepresentativePlaylist 갱신
     */
    private void replaceRepresentativePlaylist(Users user, Playlist newRepPlaylist) {
        String userId = user.getId();

        // 1. 기존 대표 isRepresentative false 처리
        playlistRepository.clearRepresentativeByUserId(userId);

        // 2. 새 대표 true 설정
        if (newRepPlaylist.isRepresentative()) {
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
