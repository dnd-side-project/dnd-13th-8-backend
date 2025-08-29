package com.example.demo.domain.playlist.service;

import com.example.demo.domain.playlist.repository.PlaylistRepository;
import com.example.demo.domain.user.entity.Users;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlaylistDeleteService {

    private final PlaylistRepository playlistRepository;
    private final PlaylistSaveService playlistSaveService;

    /**
     * 삭제된 대표 플레이리스트를 대체할 새 대표를 지정합니다.
     */
    public void assignNewRepresentativeIfNecessary(String userId, Long deletedPlaylistId) {
        log.info("🔁 대표 삭제됨 → 새 대표 지정 시도: userId={}, deletedPlaylistId={}", userId, deletedPlaylistId);

        playlistRepository.findMostRecentExcluding(userId, deletedPlaylistId)
                .ifPresentOrElse(
                        newRepPlaylist -> {
                            log.info("✅ 새 대표 후보 찾음: playlistId={}, name={}",
                                    newRepPlaylist.getId(), newRepPlaylist.getName());

                            Users user = newRepPlaylist.getUsers();
                            newRepPlaylist.changeToRepresentative();
                            playlistSaveService.replaceRepresentativePlaylist(user, newRepPlaylist);

                            log.info("🏅 대표 플레이리스트 교체 완료: userId={}, newRepId={}", userId, newRepPlaylist.getId());
                        },
                        () -> log.warn("⚠️ 새 대표 후보 없음! userId={}, 삭제된 playlistId={}", userId, deletedPlaylistId)
                );
    }
}
