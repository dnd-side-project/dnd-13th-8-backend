package com.example.demo.controller;


import com.example.demo.dto.ChatInbound;
import com.example.demo.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;


@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    // SEND /chat/app/rooms/{roomId}
    @MessageMapping("/rooms/{roomId}")
    public void onMessage(@DestinationVariable String roomId, @Valid @Payload ChatInbound chatInbound) {
        chatService.handleInbound(roomId, chatInbound);
    }
}
