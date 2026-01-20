package com.example.demo.global.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompJwtChannelInterceptor stompJwtChannelInterceptor;

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompJwtChannelInterceptor);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // ALB가 /chat/* 만 라우팅하므로 WS 엔드포인트도 /chat/ws 로 노출
        registry.addEndpoint("/chat/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS(); // 필요 없으면 제거 가능
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 구독 경로와 앱 전송 경로 모두 /chat 아래로
        registry.enableSimpleBroker("/chat/topic");
        registry.setApplicationDestinationPrefixes("/chat/app");
    }
}
