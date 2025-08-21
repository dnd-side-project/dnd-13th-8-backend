package com.example.demo.dto;

import com.example.demo.entity.Chat;

import java.time.Instant;

public class ChatMapper {

    public static Chat toEntity(ChatOutbound out) {
        String sentAt = Instant.now().toString();;
        return Chat.builder()
                .roomId(out.getRoomId())
                .sentAt(sentAt)
                .messageId(out.getMessageId())
                .senderId(out.getSenderId())
                .content(out.getContent())
                .systemMessage(out.isSystemMessage())
                .build();
    }
}
