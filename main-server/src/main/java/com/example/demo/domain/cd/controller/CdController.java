package com.example.demo.domain.cd.controller;

import com.example.demo.domain.cd.dto.response.CdResponse;
import com.example.demo.domain.cd.service.CdService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cd")
@RequiredArgsConstructor
public class CdController {

    private final CdService cdService;

    @GetMapping("/{playlistId}")
    public ResponseEntity<CdResponse> getSingleCd (@PathVariable("playlistId") Long playListId) {
        return ResponseEntity.ok().body(cdService.getCdByPlayListId(playListId));
    }
}
