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

    public static ChatOutbound toOutbound(Chat chat) {
        return ChatOutbound.builder()
                .roomId(chat.getRoomId())
                .sentAt(chat.getSentAt())
                .messageId(chat.getMessageId())
                .senderId(chat.getSenderId())
                .username(chat.getUsername())
                .content(chat.getContent())
                .profileImage(chat.getProfileImage())
                .systemMessage(chat.isSystemMessage())
                .build();
    }
}
