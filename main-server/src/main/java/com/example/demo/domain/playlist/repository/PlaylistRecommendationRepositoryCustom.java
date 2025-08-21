package com.example.demo.domain.playlist.repository;

import com.example.demo.domain.playlist.entity.Playlist;
import java.util.List;

public interface PlaylistRecommendationRepositoryCustom {

    public List<Playlist> findRecommendedPlaylistsByUser(String userId, int limit);
}
