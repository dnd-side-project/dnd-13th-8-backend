package com.example.demo.domain.user.controller;

import com.example.demo.domain.user.dto.UpdateProfileRequest;
import com.example.demo.domain.user.dto.UpdateProfileResponse;
import com.example.demo.domain.user.service.UsersService;
import com.example.demo.global.security.filter.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
            summary = "프로필 변경",
            description = "프로필 이름과 사진을 변경합니다"
    )
    @ApiResponse(content = @Content(schema = @Schema(implementation = UpdateProfileResponse.class)))
    @PatchMapping("/profile")
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
}
