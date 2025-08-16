package com.example.demo.domain.cd.service;

import com.example.demo.domain.cd.dto.response.CdItemResponse;
import com.example.demo.domain.cd.dto.response.CdResponse;
import com.example.demo.domain.cd.repository.CdRepository;
import com.example.demo.domain.cd.repository.projection.CdItemView;
import com.example.demo.global.r2.R2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CdService {

    // CdItem -> Cd -> CdList (CdItemList = Cd)

    private final R2Service r2Service;
    private final CdRepository cdRepository;

    public List<CdItemResponse> findAllCdItemOnCd (Long playlistId) {
        List<CdItemView> cdItemViewList = cdRepository.findAllWithImageKeys(playlistId);
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

    public CdResponse getCdByPlayListId (Long playlistId) {
        return CdResponse.builder()
                .playListId(playlistId)
                .cdItems(findAllCdItemOnCd(playlistId))
                .build();
    }
}
