package com.example.demo.service;

import com.example.demo.dto.ChatInbound;
import com.example.demo.dto.ChatMapper;
import com.example.demo.dto.ChatOutbound;
import com.example.demo.entity.Chat;
import com.example.demo.entity.repository.ChatRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final ChatRepository chatRepository;

    @Value("${chat.redis.topic-prefix:chat.room.}")
    private String topicPrefix;

    public void handleInbound(String roomId, ChatInbound chatInbound) {

        // 1) 브로드캐스트용 아웃바운드 DTO 구성
        ChatOutbound chatOutbound = ChatOutbound.builder()
                .roomId(roomId)
                .messageId(UUID.randomUUID().toString())
                .senderId(chatInbound.getSenderId())
                .content(chatInbound.getContent())
                .sentAt(Instant.now().toString())
                .systemMessage(chatInbound.isSystemMessage())
                .build();

        // 2) Redis Pub/Sub 발행 (모든 인스턴스가 수신)
        try {
            String channel = topicPrefix + roomId;     // ex) chat.room.room-1
            String payload = objectMapper.writeValueAsString(chatOutbound);
            stringRedisTemplate.convertAndSend(channel, payload);
        } catch (Exception e) {
            throw new RuntimeException("Redis 발행 실패", e);
        }

        // 3) DynamoDB 저장
        try {
            chatRepository.save(ChatMapper.toEntity(chatOutbound));
        } catch (Exception e) {
            throw new RuntimeException("DynamoDB putItem failed", e);
        }
    }

    public List<ChatOutbound> loadRecent(String roomId, String before, int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 50); // 1~50 제한
        return chatRepository.queryRecent(roomId, before, safeLimit)
                .stream()
                .map(chat -> ChatOutbound.builder()
                        .roomId(chat.getRoomId())
                        .messageId(chat.getMessageId())
                        .senderId(chat.getSenderId())
                        .content(chat.getContent())
                        .sentAt(chat.getSentAt())
                        .systemMessage(chat.isSystemMessage())
                        .build())
                .toList();
    }
}