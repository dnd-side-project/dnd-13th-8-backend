package com.example.demo.service;

import com.example.common.error.code.UserErrorCode;
import com.example.common.error.exception.UserException;
import com.example.demo.dto.ChatHistoryResponseDto;
import com.example.demo.dto.ChatInbound;
import com.example.demo.dto.ChatMapper;
import com.example.demo.dto.ChatOutbound;
import com.example.demo.entity.Chat;
import com.example.demo.entity.Users;
import com.example.demo.entity.repository.ChatRepository;
import com.example.demo.entity.repository.UsersRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final ChatRepository chatRepository;
    private final UsersRepository usersRepository;

    @Value("${chat.redis.topic-prefix:chat.room.}")
    private String topicPrefix;

    public void handleInbound(String roomId, ChatInbound chatInbound) {

        Users users = usersRepository.findById(chatInbound.getSenderId())
                .orElseThrow(()-> new UserException(UserErrorCode.USER_NOT_FOUND));

        // 1) 브로드캐스트용 아웃바운드 DTO 구성
        ChatOutbound chatOutbound = ChatOutbound.builder()
                .roomId(roomId)
                .messageId(UUID.randomUUID().toString())
                .senderId(chatInbound.getSenderId())
                .username(chatInbound.getUsername())
                .content(chatInbound.getContent())
                .sentAt(Instant.now().toString())
                .profileImage(users.getProfileUrl())
                .systemMessage(chatInbound.isSystemMessage())
                .build();

        // 2) Redis Pub/Sub 발행 (모든 인스턴스가 수신)
        try {
            String channel = topicPrefix + "room." + roomId;     // ex) chat.room.room-1
            String payload = objectMapper.writeValueAsString(chatOutbound);
            stringRedisTemplate.convertAndSend(channel, payload);
        } catch (Exception e) {
            throw new RuntimeException("Redis 발행 실패", e);
        }

        // 3) DynamoDB 저장
        try {
            chatRepository.save(ChatMapper.toEntity(chatOutbound));
        } catch (Exception e) {
            throw new RuntimeException("DB 저장 실패", e);
        }
    }

    public ChatHistoryResponseDto loadRecent(String roomId, String before, int limit) {
        int pageSize = Math.min(Math.max(limit, 1), 50); // 1~50 가드

        var slice = chatRepository.queryRecentSlice(roomId, before, pageSize);

        var messages = slice.items().stream()
                .map(chat -> ChatOutbound.builder()
                        .roomId(chat.getRoomId())
                        .messageId(chat.getMessageId())
                        .senderId(chat.getSenderId())
                        .username(chat.getUsername())
                        .content(chat.getContent())
                        .sentAt(chat.getSentAt())
                        .profileImage(chat.getProfileImage())
                        .systemMessage(chat.isSystemMessage())
                        .build()
                ).toList();

        return ChatHistoryResponseDto.builder()
                .messages(messages)
                .nextCursor(slice.nextCursor()) // 마지막 페이지면 null
                .build();
    }

    public void deleteMessage(String roomId, String messageId, String userId) {
        Chat chat = chatRepository.findOneByMessageId(roomId, messageId)
                .orElseThrow(() -> new IllegalArgumentException("Chat not found"));

        // 2) 본인 확인
        if (!userId.equals(chat.getSenderId())) {
            throw new IllegalStateException("id 불일치");
        }

        // 3) PK(roomId) + SK(sentAt)로 삭제
        boolean ok = chatRepository.deleteByPk(roomId, chat.getSentAt());
        if (!ok) {
            throw new IllegalStateException("삭제 실패");
        }
    }

    public int countByRoomId(String roomId) {
        return chatRepository.countByRoomId(roomId);
    }

    public void deleteAllByRoomId(String roomId) {
        // 멱등성: 존재하지 않아도 예외 없이 통과
        chatRepository.deleteAllByRoomId(roomId);
    }
}