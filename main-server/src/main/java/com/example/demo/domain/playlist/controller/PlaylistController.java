package com.example.demo.domain.playlist.controller;



import com.example.demo.domain.playlist.dto.PlaylistDetailResponse;
import com.example.demo.domain.playlist.dto.PlaylistResponse;
import com.example.demo.domain.playlist.dto.PlaylistSortOption;
import com.example.demo.domain.playlist.service.PlaylistService;
import com.example.demo.global.security.filter.CustomUserDetails;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/main/playlists")
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistService playlistService;

    @GetMapping("/me")
    public ResponseEntity<List<PlaylistResponse>> getMyPlaylists(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(defaultValue = "POPULAR") PlaylistSortOption sort
    ) {
        List<PlaylistResponse> myPlaylistsSorted = playlistService.getMyPlaylistsSorted(user.getId(), sort);
        return ResponseEntity.ok(myPlaylistsSorted);
    }

    @GetMapping("/{playlistId}")
    public ResponseEntity<PlaylistDetailResponse> getPlaylistDetail(
            @PathVariable Long playlistId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        PlaylistDetailResponse response = playlistService.getPlaylistDetail(user.getId(), playlistId);
        return ResponseEntity.ok(response);
    }

}
