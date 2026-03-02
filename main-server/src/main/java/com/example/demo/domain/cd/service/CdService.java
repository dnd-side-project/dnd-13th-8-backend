package com.example.demo.domain.cd.service;

import com.example.common.error.code.PlaylistErrorCode;
import com.example.common.error.code.PropErrorCode;
import com.example.common.error.exception.PlaylistException;
import com.example.common.error.exception.PropException;
import com.example.demo.domain.cd.dto.response.CdItem;
import com.example.demo.domain.cd.dto.request.CdItemRequest;
import com.example.demo.domain.cd.dto.response.*;
import com.example.demo.domain.cd.entity.Cd;
import com.example.demo.domain.cd.repository.CdRepository;
import com.example.demo.domain.cd.repository.projection.CdItemView;
import com.example.demo.domain.playlist.entity.Playlist;
import com.example.demo.domain.playlist.repository.PlaylistRepository;
import com.example.demo.domain.prop.entity.Prop;
import com.example.demo.domain.prop.repository.PropRepository;
import com.example.demo.global.r2.R2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CdService {

    private final R2Service r2Service;
    private final CdRepository cdRepository;
    private final PlaylistRepository playlistRepository;
    private final PropRepository propRepository;

    @Transactional(readOnly = true)
    public List<CdItem> findAllCdItemOnCd(Long playlistId) {
        List<CdItemView> views = cdRepository.findAllByPlaylistWithImageKeys(playlistId);
        Map<String, String> imageKeyCache = new HashMap<>();

        List<CdItem> responses = new ArrayList<>();
        for (CdItemView view : views) {
            responses.add(toCdItem(view, imageKeyCache));
        }
        return responses;
    }

    @Transactional(readOnly = true)
    public GetCdResponse getCdByPlaylistId(Long playlistId) {
        return GetCdResponse.builder()
                .playlistId(playlistId)
                .cdItems(findAllCdItemOnCd(playlistId))
                .build();
    }

    @Transactional(readOnly = true)
    public CdResponse getCdItemsByPlaylistId(Long playlistId) { // 다른 도메인에서 호출해서 사용하는 메소드
        return CdResponse.builder()
                .cdItems(findAllCdItemOnCd(playlistId))
                .build();
    }

    @Transactional(readOnly = true)
    public CdItemsByPlaylist findCdItemsByPlaylistIdsIn(List<Long> playlistIdList) {
        if (playlistIdList == null || playlistIdList.isEmpty()) {
            return CdItemsByPlaylist.empty();
        }

        List<CdItemView> views = cdRepository.findAllByPlaylistIdWithImageKeysIn(playlistIdList);
        Map<String, String> imageKeyCache = new HashMap<>();

        Map<Long, List<CdItem>> grouped = new LinkedHashMap<>();
        for (CdItemView view : views) {
            Long playlistId = view.getPlaylistId();
            CdItem cdItem = toCdItem(view, imageKeyCache);

            grouped.computeIfAbsent(playlistId, k -> new ArrayList<>()).add(cdItem);
        }

        return new CdItemsByPlaylist(grouped);
    }

    @Transactional
    public void saveCdItemList(Long playlistId, List<CdItemRequest> cdItemRequestList) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new PlaylistException(PlaylistErrorCode.PLAYLIST_NOT_FOUND));

        Set<Long> propIds = cdItemRequestList.stream()
                .map(CdItemRequest::propId)
                .collect(Collectors.toSet());

        Map<Long, Prop> propMap = propRepository.findAllById(propIds).stream()
                .collect(Collectors.toMap(Prop::getId, Function.identity()));

        List<Cd> cdList = cdItemRequestList.stream()
                .map(req -> {
                    Prop prop = propMap.get(req.propId());
                    if (prop == null) {
                        throw new PropException(PropErrorCode.PROP_NOT_FOUND);
                    }
                    return CdMapper.toEntity(playlist, prop, req);
                })
                .toList();


        cdRepository.saveAll(cdList);
    }

    @Transactional
    public void replaceCdItemList(Long playlistId, List<CdItemRequest> cdItemRequestList) {
        playlistRepository.findById(playlistId)
                .orElseThrow(() -> new PlaylistException(PlaylistErrorCode.PLAYLIST_NOT_FOUND));

        cdRepository.deleteByPlaylistId(playlistId);
        saveCdItemList(playlistId, cdItemRequestList);
    }

    private CdItem toCdItem(CdItemView view, Map<String, String> imageKeyCache) {
        String imageUrl = resolveImageUrl(view.getImageKey(), imageKeyCache);
        return CdItem.from(view, imageUrl);
    }

    private String resolveImageUrl(String imageKey, Map<String, String> cache) {
        if ("DEFAULT".equalsIgnoreCase(imageKey)) {
            return "DEFAULT";
        }
        if (imageKey != null && !imageKey.isBlank()) {
            return cache.computeIfAbsent(imageKey, r2Service::getPublicUrl);
        }
        return null;
    }
}
