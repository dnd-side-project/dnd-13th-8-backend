package com.example.demo.domain.browse.dto;

import com.example.demo.domain.cd.dto.response.CdItemResponse;
import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.song.dto.SongMapper;
import com.example.demo.domain.song.entity.Song;
import com.example.demo.domain.user.entity.Users;
import java.util.List;

public class BrowsePlaylistMapper {

    public static BrowsePlaylistDto toDto(
            Playlist playlist,
            List<Song> songs,
            CdItemResponse cdItem,
            String shareUrl,
            String totalTime
    ) {
        Users owner = playlist.getUsers();

        return new BrowsePlaylistDto(
                playlist.getId(),
                playlist.getName(),
                playlist.getGenre().getDisplayName(),
                new CreatorDto(owner.getId(), owner.getUsername()),
                SongMapper.mapPreviewSongs(songs),
                playlist.isRepresentative(),
                shareUrl,
                cdItem,
                totalTime
        );
    }
}
