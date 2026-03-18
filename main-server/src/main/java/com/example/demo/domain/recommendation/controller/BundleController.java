package com.example.demo.domain.recommendation.controller;

import com.example.demo.domain.recommendation.dto.bundle.*;
import com.example.demo.domain.recommendation.service.bundle.BundleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/main/bundle")
@RequiredArgsConstructor
@Tag(name = "Bundle", description = "플레이리스트 모음집 관리 API")
@SecurityRequirement(name = "bearerAuth")
public class BundleController {

    private final BundleService bundleService;

    @Operation(
            summary = "모음집 생성",
            description = "시간대와 제목을 입력하여 새로운 플레이리스트 모음집을 생성합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "생성된 모음집 정보",
            content = @Content(schema = @Schema(implementation = CreateBundleResponse.class))
    )
    @PostMapping()
    public ResponseEntity<CreateBundleResponse> createCollection(
            @RequestBody CreateBundleRequest request
    ) {
        CreateBundleResponse response = bundleService.createBundle(request);
        return ResponseEntity.ok().body(response);
    }

    @Operation(
            summary = "모음집에 플레이리스트 추가",
            description = "특정 모음집에 여러 플레이리스트를 순서와 함께 추가합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "추가된 플레이리스트 정보",
            content = @Content(schema = @Schema(implementation = AddBundlePlaylistResponse.class))
    )
    @PostMapping("/{bundleId}")
    public ResponseEntity<AddBundlePlaylistResponse> addPlaylists(
            @PathVariable Long bundleId,
            @RequestBody AddBundlePlaylistRequest request
    ) {
        AddBundlePlaylistResponse response = bundleService.addPlaylists(bundleId, request);
        return ResponseEntity.ok().body(response);
    }

    @Operation(
            summary = "모음집 삭제",
            description = "모음집을 삭제합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "모음집 삭제 성공"
    )
    @DeleteMapping("/{bundleId}")
    public ResponseEntity<String> deleteBundle(@PathVariable Long bundleId) {
        bundleService.deleteBundle(bundleId);
        return ResponseEntity.ok("삭제 성공");
    }

    @Operation(
            summary = "모음집 조회",
            description = "모음집과 해당 모음집에 포함된 플레이리스트 ID, 제목을 조회합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "모음집 조회 성공",
            content = @Content(schema = @Schema(implementation = GetBundleResponse.class))
    )
    @GetMapping("/{bundleId}")
    public ResponseEntity<GetBundleResponse> getBundle(@PathVariable Long bundleId) {
        GetBundleResponse response = bundleService.getBundle(bundleId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "모음집 전체 조회",
            description = "모든 모음집과 해당 모음집에 포함된 플레이리스트 ID, 제목을 조회합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "모음집 전체 조회 성공",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = GetAllBundlesResponse.class)))
    )
    @GetMapping()
    public ResponseEntity<List<GetAllBundlesResponse>> getAllBundles() {
        List<GetAllBundlesResponse> response = bundleService.getAllBundles();
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "플레이리스트 목록 전체 조회",
            description = "플레이리스트 목록 전체를 조회합니다"
    )
    @ApiResponse(
            responseCode = "200",
            description = "플레이리스트 목록 전체 조회 성공",
            content = @Content(schema = @Schema(implementation = GetAllPlaylistsResponse.class))
    )
    @GetMapping("/playlist")
    public ResponseEntity<GetAllPlaylistsResponse> getAllPlaylists() {
        GetAllPlaylistsResponse response = bundleService.getAllPlaylists();
        return ResponseEntity.ok(response);
    }
}
