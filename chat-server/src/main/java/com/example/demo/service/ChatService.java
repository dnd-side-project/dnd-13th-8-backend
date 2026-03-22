package com.example.demo.service;

import com.example.common.discord.service.DiscordWebhookService;
import com.example.demo.dto.ChatHistoryResponse;
import com.example.demo.dto.ChatUserProfile;
import com.example.demo.dto.ReportChatRequest;
import com.example.demo.dto.chat.ChatInbound;
import com.example.demo.dto.chat.ChatMapper;
import com.example.demo.dto.chat.ChatOutbound;
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
    private final ChatProfileService chatProfileService;
    private final DiscordWebhookService discordWebhookService;
    private final UsersRepository usersRepository;

    @Value("${chat.redis.topic-prefix:chat.room.}")
    private String topicPrefix;

    public void handleInbound(String roomId, ChatInbound chatInbound, String userId,
                              ChatUserProfile sessionProfile) {

        ChatUserProfile profile = sessionProfile;
        if (profile == null) {
            profile = chatProfileService.getOrLoad(userId);
        }

        // 1) 브로드캐스트용 아웃바운드 DTO 구성
        ChatOutbound chatOutbound = ChatOutbound.builder()
                .roomId(roomId)
                .messageId(UUID.randomUUID().toString())
                .senderId(userId)
                .username(profile.username())
                .content(chatInbound.getContent())
                .sentAt(Instant.now().toString())
                .profileImage(profile.profileImage())
                .systemMessage(chatInbound.isSystemMessage())
                .build();

        // 2) DynamoDB 저장
        try {
            chatRepository.saveAndIncrementCount(ChatMapper.toEntity(chatOutbound));
        } catch (Exception e) {
            throw new RuntimeException("DB 저장 실패", e);
        }

        // 3) Redis Pub/Sub 발행 (모든 인스턴스가 수신)
        try {
            String channel = topicPrefix + "room." + roomId;     // ex) chat.room.room-1
            String payload = objectMapper.writeValueAsString(chatOutbound);
            stringRedisTemplate.convertAndSend(channel, payload);
        } catch (Exception e) {
            throw new RuntimeException("Redis 발행 실패", e);
        }
    }

    public ChatHistoryResponse loadRecent(String roomId, String before, int limit) {
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

        return ChatHistoryResponse.builder()
                .messages(messages)
                .nextCursor(slice.nextCursor()) // 마지막 페이지면 null
                .build();
    }

    public void deleteMessage(String roomId, String messageId, String userId) {
        Chat chat = chatRepository.findOneByMessageId(roomId, messageId)
                .orElseThrow(() -> new IllegalArgumentException("Chat not found"));

        // 3) PK(roomId) + SK(sentAt)로 삭제
        chatRepository.deleteAndDecrementCount(roomId, chat.getSentAt());
    }

    public int countByRoomId(String roomId) {
        return chatRepository.countByRoomId(roomId);
    }

    public void deleteAllByRoomId(String roomId) {
        chatRepository.deleteAllByRoomId(roomId);
    }

    public void reportMessage(
            String roomId,
            String messageId,
            ReportChatRequest request,
            String reporterId
    ) {
        Chat chat = chatRepository.findOneByMessageId(roomId, messageId)
                .orElseThrow(() -> new IllegalArgumentException("채팅을 찾을 수 없습니다."));

        Users reporter = usersRepository.findById(reporterId)
                .orElseThrow(() -> new IllegalArgumentException("신고자를 찾을 수 없습니다."));

        Users writer = usersRepository.findById(chat.getSenderId())
                .orElseThrow(() -> new IllegalArgumentException("작성자를 찾을 수 없습니다."));

        String content = request.content();
        if (content != null && content.length() > 300) {
            content = content.substring(0, 300) + "...";
        }

        String message = """
            🚨 채팅 신고 접수

            - 플레이리스트: %s
            - 채팅 내용: %s
            - 신고자: %s (%s)
            - 작성자: %s (%s)
            """.formatted(
                request.playlistName(),
                content == null ? "-" : content,
                reporter.getUsername(),
                reporter.getId(),
                writer.getUsername(),
                writer.getId()
        );

        discordWebhookService.sendMessage(message);
    }
}