package com.example.demo.domain.cd.service;

import com.example.common.error.code.PlaylistErrorCode;
import com.example.common.error.code.PropErrorCode;
import com.example.common.error.exception.PlaylistException;
import com.example.common.error.exception.PropException;
import com.example.demo.domain.cd.dto.request.CdItemRequest;
import com.example.demo.domain.cd.dto.response.CdItemResponse;
import com.example.demo.domain.cd.dto.response.CdListResponseDto;
import com.example.demo.domain.cd.dto.response.CdResponse;
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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CdService {

    // CdItem -> Cd -> CdList (CdItemList = Cd)

    private final R2Service r2Service;
    private final CdRepository cdRepository;
    private final PlaylistRepository playlistRepository;
    private final PropRepository propRepository;

    public List<CdItemResponse> findAllCdItemOnCd (Long playlistId) {
        List<CdItemView> cdItemViewList = cdRepository.findAllByPlaylistWithImageKeys(playlistId);
        return cdItemViewList.stream().map(v -> CdItemResponse.builder()
                .cdItemId(v.cdId())
                .propId(v.propId())
                .xCoordinate(v.xCoordinate())
                .yCoordinate(v.yCoordinate())
                .zCoordinate(v.zCoordinate())
                .angle(v.angle())
                .imageUrl(r2Service.getPresignedUrl(v.imageKey()))
                .build()).toList();
    }

    public CdResponse getCdByPlaylistId (Long playlistId) {
        return CdResponse.builder()
                .playlistId(playlistId)
                .cdItems(findAllCdItemOnCd(playlistId))
                .build();
    }

    public CdListResponseDto getAllCdByPlaylistIdList (List<Long> playlistIdList) {
        if (playlistIdList == null || playlistIdList.isEmpty()) {
            return new CdListResponseDto(List.of());
        }

        List<CdItemView> cdItemViewList = cdRepository.findAllByPlaylistIdWithImageKeysIn(playlistIdList);

        var byPlaylist = cdItemViewList.stream()
                .collect(Collectors.groupingBy(
                        CdItemView::playlistId,
                        LinkedHashMap::new,
                        Collectors.mapping(r -> CdItemResponse.builder()
                                        .cdItemId(r.cdId())
                                        .propId(r.propId())
                                        .xCoordinate(r.xCoordinate())
                                        .yCoordinate(r.yCoordinate())
                                        .zCoordinate(r.zCoordinate())
                                        .angle(r.angle())
                                        .imageUrl(r2Service.getPresignedUrl(r.imageKey()))
                                        .build(),
                                Collectors.toList()
                        )
                ));
        List<CdResponse> cdResponses = byPlaylist.entrySet().stream()
                .map(entry -> CdResponse.builder()
                        .playlistId(entry.getKey())
                        .cdItems(entry.getValue())
                        .build())
                .toList();

        return new CdListResponseDto(cdResponses);
    }

    @Transactional
    public void saveCdItemList(Long playlistId, List<CdItemRequest> cdItemRequestList) {

        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new PlaylistException(PlaylistErrorCode.PLAYLIST_NOT_FOUND));

        // 필요한 propId 목록 추출
        Set<Long> propIds = cdItemRequestList.stream()
                .map(CdItemRequest::propId)
                .collect(Collectors.toSet());

        // 한 번에 조회
        Map<Long, Prop> propMap = propRepository.findAllById(propIds).stream()
                .collect(Collectors.toMap(Prop::getId, Function.identity()));

        // 변환
        List<Cd> cdList = cdItemRequestList.stream()
                .map(cd -> {
                    Prop prop = propMap.get(cd.propId());
                    if (prop == null) {
                        throw new PropException(PropErrorCode.PROP_NOT_FOUND);
                    }
                    return Cd.builder()
                            .playlist(playlist)
                            .prop(prop)
                            .xCoordinate(cd.xCoordinate())
                            .yCoordinate(cd.yCoordinate())
                            .zCoordinate(cd.zCoordinate())
                            .angle(cd.angle())
                            .build();
                })
                .toList();

        cdRepository.saveAll(cdList);
    }

    @Transactional
    public void replaceCdItemList(Long playlistId, List<CdItemRequest> cdItemRequestList) {
        playlistRepository.findById(playlistId)
                .orElseThrow(() -> new PlaylistException(PlaylistErrorCode.PLAYLIST_NOT_FOUND));

        cdRepository.deleteByPlaylist_Id(playlistId);
        saveCdItemList(playlistId, cdItemRequestList);
    }
}
