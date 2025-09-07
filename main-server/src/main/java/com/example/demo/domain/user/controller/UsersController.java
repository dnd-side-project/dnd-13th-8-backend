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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @ModelAttribute UpdateProfileRequest request // MultipartFile + String 같이 받기 위해 @ModelAttribute
    ) throws IOException {
        UpdateProfileResponse response = usersService.updateProfile(userDetails.getId(), request);
        return ResponseEntity.ok(response);
    }
}
