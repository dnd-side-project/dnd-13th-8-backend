package com.example.demo.domain.recommendation.service.bundle;

import com.example.common.error.code.PlaylistErrorCode;
import com.example.common.error.exception.PlaylistException;
import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.playlist.repository.PlaylistRepository;
import com.example.demo.domain.recommendation.dto.bundle.*;
import com.example.demo.domain.recommendation.entity.bundle.Bundle;
import com.example.demo.domain.recommendation.entity.bundle.BundlePlaylist;
import com.example.demo.domain.recommendation.repository.bundle.BundlePlaylistRepository;
import com.example.demo.domain.recommendation.repository.bundle.BundleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class BundleServiceImpl implements BundleService {

    private final BundleRepository bundleRepository;
    private final BundlePlaylistRepository bundlePlaylistRepository;
    private final PlaylistRepository playlistRepository;

    @Override
    public CreateBundleResponse createBundle(CreateBundleRequest request) {

        Bundle bundle = Bundle.builder()
                .timeSlot(request.timeSlot())
                .title(request.title())
                .build();

        Bundle savedBundle = bundleRepository.save(bundle);

        return CreateBundleResponse.from(savedBundle);
    }

    @Override
    public AddBundlePlaylistResponse addPlaylists(Long bundleId, AddBundlePlaylistRequest request) {

        Bundle bundle = bundleRepository.findById(bundleId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 번들입니다."));

        List<AddBundlePlaylistResponse.BundlePlaylistResult> results = new ArrayList<>();

        for (AddBundlePlaylistRequest.BundlePlaylistItem item : request.playlists()) {

            Long playlistId = item.playlistId();
            Integer orderIndex = item.orderIndex();

            Playlist playlist = playlistRepository.findById(playlistId)
                    .orElseThrow(() -> new PlaylistException(PlaylistErrorCode.PLAYLIST_NOT_FOUND));

            BundlePlaylist bundlePlaylist = new BundlePlaylist(bundle, playlist, orderIndex);

            bundlePlaylistRepository.save(bundlePlaylist);

            results.add(new AddBundlePlaylistResponse.BundlePlaylistResult(playlistId, orderIndex));
        }

        return new AddBundlePlaylistResponse(bundleId, results);
    }

    @Override
    public void deleteBundle(Long bundleId) {
        bundleRepository.deleteById(bundleId);
    }

    @Override
    @Transactional(readOnly = true)
    public GetBundleResponse getBundle(Long bundleId) {
        Bundle bundle = bundleRepository.findById(bundleId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 번들입니다."));

        List<BundlePlaylist> bundlePlaylists = bundlePlaylistRepository.findByBundleIdWithPlaylist(bundleId);

        List<GetBundleResponse.BundlePlaylistItem> playlists = bundlePlaylists.stream()
                .map(bundlePlaylist -> new GetBundleResponse.BundlePlaylistItem(
                        bundlePlaylist.getPlaylist().getId(),
                        bundlePlaylist.getPlaylist().getName(),
                        bundlePlaylist.getOrderIndex()
                ))
                .toList();

        return new GetBundleResponse(
                bundle.getId(),
                bundle.getTimeSlot(),
                bundle.getTitle(),
                playlists
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<GetAllBundlesResponse> getAllBundles() {
        List<Bundle> bundles = bundleRepository.findAll();

        if (bundles.isEmpty()) {
            return List.of();
        }

        List<BundlePlaylist> bundlePlaylists = bundlePlaylistRepository.findAllWithBundleAndPlaylist();

        Map<Long, List<GetAllBundlesResponse.BundlePlaylistItem>> playlistMap = new LinkedHashMap<>();

        for (BundlePlaylist bundlePlaylist : bundlePlaylists) {
            Long bundleId = bundlePlaylist.getBundle().getId();

            playlistMap.computeIfAbsent(bundleId, key -> new ArrayList<>())
                    .add(new GetAllBundlesResponse.BundlePlaylistItem(
                            bundlePlaylist.getPlaylist().getId(),
                            bundlePlaylist.getPlaylist().getName()
                    ));
        }

        return bundles.stream()
                .map(bundle -> new GetAllBundlesResponse(
                        bundle.getId(),
                        bundle.getTimeSlot(),
                        bundle.getTitle(),
                        playlistMap.getOrDefault(bundle.getId(), List.of())
                ))
                .toList();
    }
}
