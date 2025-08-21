package com.example.demo.domain.playlist.controller;

import com.example.demo.domain.playlist.dto.PlaylistCreateRequest;
import com.example.demo.domain.playlist.dto.PlaylistDetailResponse;
import com.example.demo.domain.playlist.dto.PlaylistResponse;
import com.example.demo.domain.playlist.dto.PlaylistSortOption;
import com.example.demo.domain.playlist.dto.PlaylistWithSongsResponse;
import com.example.demo.domain.playlist.service.PlaylistService;
import com.example.demo.global.security.filter.CustomUserDetails;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/main/playlists")
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistService playlistService;

    @PostMapping("/temp")
    public ResponseEntity<Void> saveTempPlaylist(
            @RequestBody @Valid PlaylistCreateRequest request,
            HttpSession session
    ) {
        session.setAttribute("tempPlaylist", request);
        return ResponseEntity.ok().build();
    }


    @PostMapping
    public ResponseEntity<PlaylistWithSongsResponse> savePlaylist(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam("theme") String theme,
            HttpSession session
    ) {
        PlaylistCreateRequest request = (PlaylistCreateRequest) session.getAttribute("tempPlaylist");
        if (request == null) {
            throw new IllegalStateException("세션에 임시 저장된 플레이리스트가 없습니다.");
        }

        PlaylistWithSongsResponse response = playlistService.savePlaylistWithSongs(user.getId(), request, theme);
        session.removeAttribute("tempPlaylist"); // 저장 후 세션 제거
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<List<PlaylistResponse>> getMyPlaylists(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(defaultValue = "POPULAR") PlaylistSortOption sort
    ) {
        List<PlaylistResponse> myPlaylistsSorted = playlistService.getMyPlaylistsSorted(user.getId(), sort);
        return ResponseEntity.ok(myPlaylistsSorted);
    }

    @GetMapping("/me/{playlistId}")
    public ResponseEntity<PlaylistDetailResponse> getPlaylistDetail(
            @PathVariable Long playlistId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        PlaylistDetailResponse response = playlistService.getPlaylistDetail(user.getId(), playlistId);
        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/me/{playlistId}")
    public ResponseEntity<Void> deletePlaylist(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long playlistId
    ) {
        playlistService.deletePlaylist(user.getId(), playlistId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/me/{playlistId}/share")
    public ResponseEntity<String> sharePlaylist(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long playlistId
    ) {
        String shareCode = playlistService.sharePlaylist(user.getId(), playlistId);
        return ResponseEntity.ok(shareCode);
    }

    @PatchMapping("/me/{playlistId}/representative")
    public ResponseEntity<Void> updateRepresentative(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long playlistId
    ) {
        playlistService.updateRepresentative(user.getId(), playlistId);
        return ResponseEntity.noContent().build();
    }

}
