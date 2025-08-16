package com.example.demo.domain.cd.controller;

import com.example.demo.domain.cd.dto.response.CdResponse;
import com.example.demo.domain.cd.service.CdService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
        return ResponseEntity.ok().body(cdService.getCdByPlayListId(playListId));
    }
}
