package com.example.demo.domain.cd.service;

import com.example.demo.domain.cd.dto.response.CdItemResponse;
import com.example.demo.domain.cd.dto.response.CdListResponseDto;
import com.example.demo.domain.cd.dto.response.CdResponse;
import com.example.demo.domain.cd.repository.CdRepository;
import com.example.demo.domain.cd.repository.projection.CdItemView;
import com.example.demo.global.r2.R2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CdService {

    // CdItem -> Cd -> CdList (CdItemList = Cd)

    private final R2Service r2Service;
    private final CdRepository cdRepository;

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
}
