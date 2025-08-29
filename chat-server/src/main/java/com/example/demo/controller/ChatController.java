package com.example.demo.controller;


import com.example.demo.dto.ChatHistoryResponseDto;
import com.example.demo.dto.ChatInbound;
import com.example.demo.dto.ChatOutbound;
import com.example.demo.global.redis.ChatRedisCounter;
import com.example.demo.global.security.filter.CustomUserDetails;
import com.example.demo.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;


@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final ChatRedisCounter chatRedisCounter;

    // SEND /chat/app/rooms/{roomId}
    @MessageMapping("/rooms/{roomId}")
    public void onMessage(@DestinationVariable String roomId, @Valid @Payload ChatInbound chatInbound) {
        chatService.handleInbound(roomId, chatInbound);
    }

    @GetMapping("/chat/rooms/{roomId}/count")
    @Operation(
            summary = "채팅방 참여자 수 조회",
            description = "해당 채팅방의 현재 참여자 수를 1회 반환합니다. (소켓 X)",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(example = "{\"roomId\":\"test-room\",\"count\":2}")
                            )
                    )
            }
    )
    public Map<String, Object> countLiveUser(@PathVariable String roomId) {
        return Map.of("roomId", roomId, "count", chatRedisCounter.getCount(roomId));
    }

    @GetMapping("/chat/rooms/{roomId}/history")
    @Operation(
            summary = "채팅방 히스토리 조회",
            description = "요청한 채팅방의 최근 메시지를 반환합니다. 기본 50개를 반환하며, `before`가 없으면 최신부터 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ChatHistoryResponseDto.class)
                            )
                    )
            }
    )
    public ResponseEntity<ChatHistoryResponseDto> history(
            @PathVariable String roomId,
            @RequestParam(required = false) String before,
            @RequestParam(defaultValue = "50") int limit
    ) {
        List<ChatOutbound> messages = chatService.loadRecent(roomId, before, limit);
        String nextCursor = messages.isEmpty() ? null : messages.getLast().getSentAt();

        ChatHistoryResponseDto chatHistoryResponseDto = ChatHistoryResponseDto.builder()
                .messages(messages)
                .nextCursor(nextCursor)
                .build();

        return ResponseEntity.ok(chatHistoryResponseDto);
    }

    @GetMapping("/chat/token")
    @Operation(
            summary = "토큰 인식 확인용"
    )
    public ResponseEntity<String> testToken(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok().body(user.getUsername());
    }
}
