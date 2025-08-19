package com.example.demo.domain.prop.controller;

import com.example.common.error.exception.DomainException;
import com.example.demo.domain.prop.dto.request.UploadPropRequestDto;
import com.example.demo.domain.prop.dto.response.GetPropListResponseDto;
import com.example.demo.domain.prop.service.PropService;
import com.example.demo.domain.user.repository.UsersRepository;
import com.example.demo.global.security.filter.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/prop")
@RequiredArgsConstructor
@Tag(name = "Prop", description = "CD 커스터마이징 Prop API")
public class PropController {

    private final PropService propService;

    @PostMapping(value = "/upload" ,consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Prop 이미지 업로드",
            description = "단일 이미지를 R2에 저장 후 Bucket Key를 DB에 저장",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(
                                    schema = @Schema(type = "string", example = "사진을 업로드하였습니다")
                            )
                    )
            }
    )
    public ResponseEntity<String> uploadImage(@ModelAttribute @Valid UploadPropRequestDto uploadPropRequestDto,
    @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        MultipartFile file = uploadPropRequestDto.file();
        String userId = customUserDetails.getId();
        propService.saveProp(userId, file);
        return ResponseEntity.ok().body("사진을 업로드하였습니다");
    }

    @GetMapping("/list")
    @Operation(
            summary = "사용자의 Prop List 조회",
            description = "사용자가 업로드 한 Prop을 전체 조회합니다",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(
                                    schema = @Schema (implementation = GetPropListResponseDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            content = @Content(
                                    schema = @Schema (implementation = DomainException.class)
                            )
                    )
            }
    )
    public ResponseEntity<GetPropListResponseDto> getPropList(@AuthenticationPrincipal CustomUserDetails customUserDetails) { // security 적용 시 AuthenticationPrincipal 변경
        return ResponseEntity.ok(propService.findPropListByUserId(customUserDetails.getId()));
    }

    @DeleteMapping("/delete")
    @Operation(
            summary = "사용자의 Prop 삭제",
            description = "사용자가 요청한 Prop (단일) 을 삭제합니다",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(
                                    schema = @Schema (implementation = String.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            content = @Content(
                                    schema = @Schema (implementation = DomainException.class)
                            )
                    )
            }
    )
    public ResponseEntity<String> deleteProp(@AuthenticationPrincipal CustomUserDetails customUserDetails, @RequestParam Long propId) {
        propService.deletePropById(customUserDetails.getId(), propId);
        return ResponseEntity.ok().body("이미지를 삭제했습니다");
    }

    @GetMapping("/check")
    @Operation(
            summary = "버킷 키로 이미지 실제 확인",
            description = "버킷 키에 해당하는 이미지 주소 리다이렉트합니다 (테스트용)"
    )
    public ResponseEntity<Void> viewImage(@RequestParam String key) { // 실제 이미지 확인용 테스트 컨트롤러
        String url = propService.getPropImageUrl(key);
        return ResponseEntity.status(302).header("Location", url).build();
    }
}
