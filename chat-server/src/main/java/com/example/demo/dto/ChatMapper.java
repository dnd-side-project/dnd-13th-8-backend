package com.example.demo.dto;

import com.example.demo.entity.Chat;

import java.time.Instant;

public class ChatMapper {

    public static Chat toEntity(ChatOutbound chatOutbound) {
        String sentAt = Instant.now().toString();
        return Chat.builder()
                .roomId(chatOutbound.getRoomId())
                .sentAt(sentAt)
                .messageId(chatOutbound.getMessageId())
                .senderId(chatOutbound.getSenderId())
                .username(chatOutbound.getUsername())
                .content(chatOutbound.getContent())
                .systemMessage(chatOutbound.isSystemMessage())
                .build();
    }
}
