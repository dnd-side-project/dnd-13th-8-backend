package com.example.demo.domain.representative.service;

import com.example.common.error.code.PlaylistErrorCode;
import com.example.common.error.exception.PlaylistException;
import com.example.demo.domain.cd.service.CdService;
import com.example.demo.domain.playlist.dto.SongDto;
import com.example.demo.domain.playlist.dto.playlistdto.PlaylistDetailResponse;
import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.representative.entity.RepresentativePlaylist;
import com.example.demo.domain.representative.repository.RepresentativePlaylistRepository;
import com.example.demo.domain.song.entity.Song;
import com.example.demo.domain.song.repository.SongRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RepresentativePlaylistService {

    private final RepresentativePlaylistRepository representativePlaylistRepository;
    private final SongRepository songRepository;
    private final CdService cdService;

    public RepresentativePlaylist findRepresentativePlaylistByUserId(String userId) {
        return representativePlaylistRepository.findByUser_Id(userId)
                .orElseThrow(()-> new PlaylistException(PlaylistErrorCode.PLAYLIST_NOT_FOUND));
    }


    @Transactional(readOnly = true)
    public PlaylistDetailResponse getMyRepresentativePlaylist(String userId) {
        RepresentativePlaylist rep = representativePlaylistRepository.findByUser_Id(userId)
                .orElseThrow(() -> new PlaylistException("대표 플레이리스트가 존재하지 않습니다.", PlaylistErrorCode.PLAYLIST_NOT_FOUND));

        Playlist playlist = rep.getPlaylist();
        List<Song> songs = songRepository.findByPlaylistId(playlist.getId());
        List<SongDto> songDtos = songs.stream()
                .map(SongDto::from)
                .toList();

        return PlaylistDetailResponse.from(playlist, songDtos, cdService.getOnlyCdByPlaylistId(playlist.getId()));
    }


}
