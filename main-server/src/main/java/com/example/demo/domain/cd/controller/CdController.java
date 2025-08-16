package com.example.demo.domain.cd.controller;

import com.example.demo.domain.cd.dto.request.GetCdListRequestDto;
import com.example.demo.domain.cd.dto.response.CdListResponseDto;
import com.example.demo.domain.cd.dto.response.CdResponse;
import com.example.demo.domain.cd.service.CdService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cd")
@RequiredArgsConstructor
public class CdController {

    private final CdService cdService;

    @GetMapping("/{playlistId}")
    @Operation(
            summary = "CD 조회",
            description = "요청한 CD (단일) 를 조회합니다",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(
                                    schema = @Schema(implementation = CdResponse.class)
                            )
                    )
            }
    )
    public ResponseEntity<CdResponse> getSingleCd (@PathVariable("playlistId") Long playListId) {
        return ResponseEntity.ok().body(cdService.getCdByPlaylistId(playListId));
    }

    @PostMapping("/list")
    @Operation(
            summary = "CD 리스트 조회",
            description = "요청한 CD들 (다중) 을 조회합니다",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(
                                    schema = @Schema(implementation = CdListResponseDto.class)
                            )
                    )
            }
    )
    public ResponseEntity<CdListResponseDto> getCdList (@RequestBody GetCdListRequestDto getCdListRequestDto) {
        List<Long> playListIdList = getCdListRequestDto.playlistIds();
        return ResponseEntity.ok().body(cdService.getAllCdByPlaylistIdList(playListIdList));
    }
}
