package com.example.demo.global.redis;

import com.example.demo.dto.ChatOutbound;
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

    //Redis Pub/Sub으로 메시지를 받았을 때 실행됨
    public void onMessage(Message message, byte[] pattern) {
        try {
            // Redis에서 전달받은 payload
            String body = new String(message.getBody());

            ChatOutbound chatOutbound = objectMapper.readValue(body, ChatOutbound.class);

            // STOMP 구독자에게 브로드캐스트 (/chat/topic/rooms/{roomId})
            String destination = "/chat/topic/rooms/" + chatOutbound.getRoomId();
            messagingTemplate.convertAndSend(destination, chatOutbound);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

