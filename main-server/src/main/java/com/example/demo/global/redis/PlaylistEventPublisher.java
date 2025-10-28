package com.example.demo.global.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PlaylistEventPublisher {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${chat.redis.topic-playlist-deleted:chat.playlist.deleted}")
    private String playlistDeletedTopic;

    public void publishPlaylistDelete(String playlistId) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("playlistId", playlistId);

            String message = objectMapper.writeValueAsString(payload);
            redisTemplate.convertAndSend(playlistDeletedTopic, message);
        } catch (Exception e) {
            //
        }
    }
}