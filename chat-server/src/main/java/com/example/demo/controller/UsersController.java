package com.example.demo.controller;

import com.example.demo.dto.GetUsernameAndIdResponse;
import com.example.demo.global.security.filter.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat/user")
public class UsersController {
    @GetMapping
    @Operation(
            summary = "내 이름/아이디 확인 (채팅용)",
            description = "로그인 된 유저의 이름 (Username)과 ID를 조회합니다",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(
                                    schema = @Schema(implementation = String.class)
                            )
                    )
            }
    )
    public ResponseEntity<GetUsernameAndIdResponse> getUsernameAndId(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        GetUsernameAndIdResponse getUsernameAndIdResponse = GetUsernameAndIdResponse.builder()
                .userId(customUserDetails.getId())
                .username(customUserDetails.getUsername())
                .build();
        return ResponseEntity.ok().body(getUsernameAndIdResponse);
    }
}
