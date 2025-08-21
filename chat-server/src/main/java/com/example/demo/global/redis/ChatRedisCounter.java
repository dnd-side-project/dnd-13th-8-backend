package com.example.demo.global.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.time.Duration;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ChatRedisCounter {

    private final StringRedisTemplate redis;
    private final SimpMessagingTemplate broker;
    private final AntPathMatcher matcher = new AntPathMatcher();

    private static final Duration MEMBERS_TTL = Duration.ofMinutes(5);

    private String keyMembers(String roomId) { return "chat:room:" + roomId + ":members"; }
    private String keySessionMap() { return "chat:sessions"; } // {sessionId -> roomId}
    private String countDestination(String roomId) { return "/chat/topic/rooms/" + roomId + "/count"; }

    private void touchTtl(String roomId) { redis.expire(keyMembers(roomId), MEMBERS_TTL); }

    private void broadcast(String roomId) {
        Long size = redis.opsForSet().size(keyMembers(roomId));
        long count = (size == null) ? 0L : size;
        broker.convertAndSend(countDestination(roomId), Map.of("roomId", roomId, "count", count));
    }

    @EventListener
    public void onSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor acc = StompHeaderAccessor.wrap(event.getMessage());
        String dest = acc.getDestination(); if (dest == null) return;
        if (!matcher.match("/chat/topic/rooms/{roomId}", dest)) return;

        String roomId = matcher.extractUriTemplateVariables("/chat/topic/rooms/{roomId}", dest).get("roomId");
        String sessionId = acc.getSessionId(); if (roomId == null || sessionId == null) return;

        // 한 세션은 한 방만: 이전 매핑이 있으면 먼저 정리
        Object prev = redis.opsForHash().get(keySessionMap(), sessionId);
        if (prev != null) {
            String prevRoom = prev.toString();
            redis.opsForSet().remove(keyMembers(prevRoom), sessionId);
            broadcast(prevRoom);
        }

        // 현재 방에 등록 + 세션→방 매핑 저장
        redis.opsForSet().add(keyMembers(roomId), sessionId);
        redis.opsForHash().put(keySessionMap(), sessionId, roomId);
        touchTtl(roomId);
        broadcast(roomId);
    }

    @EventListener
    public void onUnsubscribe(SessionUnsubscribeEvent event) {
        StompHeaderAccessor acc = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = acc.getSessionId(); if (sessionId == null) return;

        Object mapped = redis.opsForHash().get(keySessionMap(), sessionId);
        if (mapped == null) return;
        String roomId = mapped.toString();

        redis.opsForSet().remove(keyMembers(roomId), sessionId);
        redis.opsForHash().delete(keySessionMap(), sessionId);
        touchTtl(roomId);
        broadcast(roomId);
    }

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor acc = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = acc.getSessionId(); if (sessionId == null) return;

        Object mapped = redis.opsForHash().get(keySessionMap(), sessionId);
        if (mapped == null) return;
        String roomId = mapped.toString();

        redis.opsForSet().remove(keyMembers(roomId), sessionId);
        redis.opsForHash().delete(keySessionMap(), sessionId);
        touchTtl(roomId);
        broadcast(roomId);
    }

    public long getCount(String roomId) {
        Long size = redis.opsForSet().size(keyMembers(roomId));
        return size == null ? 0L : size;
    }
}