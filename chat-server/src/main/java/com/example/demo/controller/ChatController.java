package com.example.demo.controller;


import com.example.demo.dto.ChatInbound;
import com.example.demo.global.redis.ChatRedisCounter;
import com.example.demo.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

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
    public Map<String, Object> countLiveUser(@PathVariable String roomId) {
        return Map.of("roomId", roomId, "count", chatRedisCounter.getCount(roomId));
    }
}
