package com.example.demo.domain.representative.service;

import com.example.common.error.code.PlaylistErrorCode;
import com.example.common.error.exception.PlaylistException;
import com.example.demo.domain.playlist.dto.SongDto;
import com.example.demo.domain.playlist.dto.playlistdto.PlaylistDetailResponse;
import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.representative.entity.RepresentativePlaylist;
import com.example.demo.domain.representative.repository.RepresentativeRepresentativePlaylistRepository;
import com.example.demo.domain.song.entity.Song;
import com.example.demo.domain.song.repository.SongRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RepresentativePlaylistService {

    private final RepresentativeRepresentativePlaylistRepository representativePlaylistRepository;
    private final SongRepository songRepository;

    public RepresentativePlaylist findRepresentativePlaylistByUserId(String userId) {
        return representativePlaylistRepository.findByUser_Id(userId)
                .orElseThrow(()-> new PlaylistException(PlaylistErrorCode.PLAYLIST_NOT_FOUND));
    }


    @Transactional(readOnly = true)
    public PlaylistDetailResponse getMyRepresentativePlaylist(String userId) {
        RepresentativePlaylist rep = representativePlaylistRepository.findByUser_Id(userId)
                .orElseThrow(() -> new IllegalStateException("대표 플레이리스트가 존재하지 않습니다."));

        Playlist playlist = rep.getPlaylist();
        List<Song> songs = songRepository.findByPlaylistId(playlist.getId());
        List<SongDto> songDtos = songs.stream()
                .map(SongDto::from)
                .toList();

        return PlaylistDetailResponse.from(playlist, songDtos);
    }


}
