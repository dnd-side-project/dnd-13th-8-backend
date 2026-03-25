package com.example.demo.service;

import com.example.common.discord.service.DiscordWebhookService;
import com.example.demo.dto.ChatHistoryResponse;
import com.example.demo.dto.ChatUserProfile;
import com.example.demo.dto.chat.ChatInbound;
import com.example.demo.dto.chat.ChatMapper;
import com.example.demo.dto.chat.ChatOutbound;
import com.example.demo.entity.Chat;
import com.example.demo.entity.Playlist;
import com.example.demo.entity.Users;
import com.example.demo.entity.repository.ChatRepository;
import com.example.demo.entity.repository.PlaylistRepository;
import com.example.demo.entity.repository.UsersRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final ChatRepository chatRepository;
    private final ChatProfileService chatProfileService;
    private final DiscordWebhookService discordWebhookService;
    private final UsersRepository usersRepository;
    private final PlaylistRepository playlistRepository;

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
                .shareCode(profile.shareCode())
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

    @Transactional(readOnly = true)
    public ChatHistoryResponse loadRecent(String roomId, String before, int limit) {
        int pageSize = Math.min(Math.max(limit, 1), 50);

        var slice = chatRepository.queryRecentSlice(roomId, before, pageSize);
        var chats = slice.items();

        Set<String> senderIds = chats.stream()
                .filter(chat -> !chat.isSystemMessage())
                .map(Chat::getSenderId)
                .filter(Objects::nonNull)
                .filter(id -> !id.isBlank())
                .collect(Collectors.toSet());

        Map<String, ChatUserProfile> profileMap = chatProfileService.getProfiles(senderIds);

        var messages = chats.stream()
                .map(chat -> {
                    ChatUserProfile profile = profileMap.get(chat.getSenderId());

                    String username = profile != null && profile.username() != null
                            ? profile.username()
                            : chat.getUsername();

                    String profileImage = profile != null && profile.profileImage() != null
                            ? profile.profileImage()
                            : chat.getProfileImage();

                    String shareCode = profile != null
                            ? profile.shareCode()
                            : null;

                    return ChatOutbound.builder()
                            .roomId(chat.getRoomId())
                            .messageId(chat.getMessageId())
                            .senderId(chat.getSenderId())
                            .username(username)
                            .content(chat.getContent())
                            .sentAt(chat.getSentAt())
                            .profileImage(profileImage)
                            .shareCode(shareCode)
                            .systemMessage(chat.isSystemMessage())
                            .build();
                })
                .toList();

        return ChatHistoryResponse.builder()
                .messages(messages)
                .nextCursor(slice.nextCursor())
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

    @Transactional
    public void reportMessage(String roomId, String messageId, String reporterId) {
        // 1) GSI 조회로 PK/SK 확보
        Chat indexedChat = chatRepository.findOneByMessageId(roomId, messageId)
                .orElseThrow(() -> new IllegalArgumentException("채팅을 찾을 수 없습니다."));

        // 2) 본 테이블 PK/SK 조회
        Chat chat = chatRepository.findByRoomIdAndSentAt(roomId, indexedChat.getSentAt())
                .orElseThrow(() -> new IllegalArgumentException("채팅 원문을 찾을 수 없습니다."));

        // 3) 플레이리스트 조회 (roomId = playlistId)
        Playlist playlist = playlistRepository.findById(Long.valueOf(roomId))
                .orElseThrow(() -> new IllegalArgumentException("플레이리스트를 찾을 수 없습니다."));

        // 4) 신고자 조회
        Users reporter = usersRepository.findById(reporterId)
                .orElseThrow(() -> new IllegalArgumentException("신고자를 찾을 수 없습니다."));

        // 5) 작성자 조회 (탈퇴 가능성 고려)
        Users writer = usersRepository.findById(chat.getSenderId()).orElse(null);

        String writerName = writer != null ? writer.getUsername() : chat.getUsername();

        String message = """
            🚨 채팅 신고 접수
            
            - 플레이리스트 ID: %s
            - 플레이리스트: %s
            - 채팅 내용: %s
            - 신고자: %s (%s)
            - 작성자: %s (%s)
            """.formatted(
                roomId,
                playlist.getName(),
                chat.getContent() == null ? "-" : chat.getContent(),
                reporter.getUsername(),
                reporter.getId(),
                writerName == null ? "(알 수 없음)" : writerName,
                chat.getSenderId()
        );

        discordWebhookService.sendMessage(message);
    }
}