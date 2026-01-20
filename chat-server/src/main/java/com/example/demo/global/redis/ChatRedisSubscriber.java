package com.example.demo.global.redis;

import com.example.demo.dto.chat.ChatOutbound;
import com.example.demo.dto.PlaylistDeleteEvent;
import com.example.demo.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatRedisSubscriber implements MessageListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;
    private final ChatService chatService;

    @Value("${chat.redis.topic-prefix:chat.}")
    private String topicPrefix;

    //Redis Pub/Sub으로 메시지를 받았을 때 실행됨
    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String channel = new String(message.getChannel());
            String body = new String(message.getBody());

            if (channel.equals("chat.playlist.deleted")) {
                PlaylistDeleteEvent playlistDeleteEvent = objectMapper.readValue(body, PlaylistDeleteEvent.class);
                String roomId = playlistDeleteEvent.playlistId();   // == roomId
                if (roomId != null && !roomId.isBlank()) {
                    chatService.deleteAllByRoomId(roomId);
                }
                return;
            }

            // 실시간 메시지 브로드캐스트 (chat.room.*)
            if (channel.startsWith(topicPrefix +"room.")) {
                ChatOutbound chatOutbound = objectMapper.readValue(body, ChatOutbound.class);
                String destination = "/chat/topic/rooms/" + chatOutbound.getRoomId();
                messagingTemplate.convertAndSend(destination, chatOutbound);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

