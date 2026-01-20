package com.example.demo.global.websocket;

import com.example.demo.dto.ChatUserProfile;
import com.example.demo.global.security.jwt.JwtProvider;
import com.example.demo.service.ChatProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class StompJwtChannelInterceptor implements ChannelInterceptor {

    public static final String SESSION_PROFILE_KEY = "CHAT_PROFILE"; // session attributes key

    private final JwtProvider jwtProvider;
    private final ChatProfileService chatProfileService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor acc = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (acc == null) return message;

        if (StompCommand.CONNECT.equals(acc.getCommand())) {
            String token = resolveBearerToken(acc);
            if (token == null || token.isBlank()) {
                throw new IllegalArgumentException("Missing Authorization token");
            }

            // 1) JWT 검증 + userId 추출
            String userId = jwtProvider.validateAccess(token).getPayload().getSubject();
            if (userId == null || userId.isBlank()) {
                throw new IllegalArgumentException("Invalid token subject");
            }

            // 2) Principal = userId
            acc.setUser(new UsernamePasswordAuthenticationToken(userId, null, List.of()));

            // 3) 프로필 캐시 로드(캐시 hit면 Redis에서, miss면 DB 후 Redis 저장)
            ChatUserProfile profile = chatProfileService.getOrLoad(userId);

            // 4) WebSocket 세션 attributes에 저장(같은 연결에서는 Redis도 안 탐)
            Map<String, Object> sess = acc.getSessionAttributes();
            if (sess != null) {
                sess.put(SESSION_PROFILE_KEY, profile);
            }
        }

        return message;
    }

    private String resolveBearerToken(StompHeaderAccessor acc) {
        // STOMP native header: Authorization: Bearer xxx
        List<String> authHeaders = acc.getNativeHeader("Authorization");
        if (authHeaders != null && !authHeaders.isEmpty()) {
            String v = authHeaders.get(0);
            if (v != null && v.startsWith("Bearer ")) return v.substring(7);
        }
        // 대안: access_token
        List<String> tokenHeaders = acc.getNativeHeader("access_token");
        if (tokenHeaders != null && !tokenHeaders.isEmpty()) {
            return tokenHeaders.get(0);
        }
        return null;
    }
}