package com.example.demo.domain.user.controller;

import com.example.demo.domain.user.dto.request.UpdateProfileRequest;
import com.example.demo.domain.user.dto.response.GetFeedProfileResponse;
import com.example.demo.domain.user.dto.response.IsFeedOwnerResponse;
import com.example.demo.domain.user.dto.response.UpdateProfileResponse;
import com.example.demo.domain.user.service.UsersService;
import com.example.demo.global.security.filter.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/main/user")
@RequiredArgsConstructor
@Tag(name = "User", description = "유저 정보 관리 API")
@SecurityRequirement(name = "bearerAuth")
public class UsersController {

    private final UsersService usersService;

    @Operation(
            summary = "프로필 수정",
            description = "프로필 정보를 수정합니다"
    )
    @ApiResponse(content = @Content(schema = @Schema(implementation = UpdateProfileResponse.class)))
    @PatchMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UpdateProfileResponse> updateProfile(
            @AuthenticationPrincipal CustomUserDetails me,
            @ModelAttribute UpdateProfileRequest request // MultipartFile + String 같이 받기 위해 @ModelAttribute
    ) throws IOException {
        UpdateProfileResponse response = usersService.updateProfile(me.getId(), request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "계정 탈퇴",
            description = "계정 정보를 삭제하고, 서비스에서 탈퇴합니다"
    )
    @ApiResponse(content = @Content(schema = @Schema(implementation = String.class)))
    @DeleteMapping("/account")
    public ResponseEntity<String> deleteMyAccount(
            @AuthenticationPrincipal CustomUserDetails me
    ) {
        usersService.deleteAccount(me.getId());
        return ResponseEntity.ok("서비스에서 탈퇴하였습니다");
    }

    @Operation(
            summary = "피드 프로필 조회",
            description = "피드 프로필 정보를 조회합니다"
    )
    @ApiResponse(content = @Content(schema = @Schema(implementation = UpdateProfileResponse.class)))
    @GetMapping(value = "/profile/{shareCode}")
    public ResponseEntity<GetFeedProfileResponse> getFeedProfile(
            @PathVariable String shareCode) {
        GetFeedProfileResponse response = usersService.getFeedProfileByShareCode(shareCode);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "피드 본인 확인",
            description = "본인 피드인지 인증합니다"
    )
    @ApiResponse(content = @Content(schema = @Schema(implementation = IsFeedOwnerResponse.class)))
    @GetMapping(value = "/profile/{shareCode}/owner")
    public ResponseEntity<IsFeedOwnerResponse> isUserFeedOwner(
            @AuthenticationPrincipal CustomUserDetails me,
            @PathVariable String shareCode) {
        IsFeedOwnerResponse response = usersService.isUserFeedOwner(me.getId(),shareCode);
        return ResponseEntity.ok(response);
    }
}
