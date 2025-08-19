package com.example.demo.domain.cd.controller;

import com.example.demo.domain.cd.dto.request.GetCdListRequestDto;
import com.example.demo.domain.cd.dto.request.SaveCdRequestDto;
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

    @PostMapping("/{playlistId}")
    @Operation(
            summary = "CD 저장",
            description = "커스터마이징 한 CD를 저장합니다",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "playlistId, cdItems",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = SaveCdRequestDto.class)
                    )
            ),

            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(
                                    schema = @Schema(implementation = String.class)
                            )
                    )
            }
    )
    public ResponseEntity<String> saveCd (@PathVariable("playlistId") Long playlistId, @RequestBody SaveCdRequestDto saveCdRequestDto) {
        cdService.saveCdItemList(playlistId, saveCdRequestDto.cdItems());
        return ResponseEntity.ok().body("CD가 저장되었습니다");
    }

    @PutMapping("/{playlistId}")
    @Operation(
            summary = "CD 수정 (Replace)",
            description = "수정 시 이전 CdItem은 삭제하고 요청 받은 CdItem을 저장합니다",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(
                                    schema = @Schema(implementation = String.class)
                            )
                    )
            }
    )
    public ResponseEntity<String> replaceCd (@PathVariable("playlistId") Long playlistId, @RequestBody SaveCdRequestDto saveCdRequestDto) {
        cdService.replaceCdItemList(playlistId,saveCdRequestDto.cdItems());
        return  ResponseEntity.ok().body("CD가 수정되었습니다");
    }
}
