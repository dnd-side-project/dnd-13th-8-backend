package com.example.demo.domain.recommendation.service.bundle;

import com.example.demo.domain.recommendation.dto.bundle.*;

import java.util.List;

public interface BundleService {

    CreateBundleResponse createBundle(CreateBundleRequest request);
    AddBundlePlaylistResponse addPlaylists(Long bundleId, AddBundlePlaylistRequest request);
    void deleteBundle(Long bundleId);
    GetBundleResponse getBundle(Long bundleId);
    List<GetAllBundlesResponse> getAllBundles();
    GetAllPlaylistsResponse getAllPlaylists();
}
