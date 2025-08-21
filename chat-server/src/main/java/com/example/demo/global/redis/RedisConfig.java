package com.example.demo.global.redis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Configuration
public class RedisConfig {
    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${spring.data.redis.password}")
    private String redisPassword;

    @Value("${spring.data.redis.ssl.enabled}")
    private boolean useSsl;

    @Value("${chat.redis.topic-prefix}")
    private String topicPrefix;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // Standalone 설정 (클러스터/리플리카 셋업이 아니면 보통 이걸로 충분)
        RedisStandaloneConfiguration standalone = new RedisStandaloneConfiguration(redisHost, redisPort);
        if (redisPassword != null && !redisPassword.isEmpty()) {
            standalone.setPassword(RedisPassword.of(redisPassword));
        }

        // Lettuce 클라이언트 옵션 + SSL 여부
        LettuceClientConfiguration.LettuceClientConfigurationBuilder clientBuilder =
                LettuceClientConfiguration.builder();

        if (useSsl) {
            clientBuilder.useSsl();
        }

        LettuceClientConfiguration clientConfig = clientBuilder.build();
        return new LettuceConnectionFactory(standalone, clientConfig);
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory cf) {
        return new StringRedisTemplate(cf);
    }

    // 모든 room 채널을 패턴 구독
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory cf,
            MessageListenerAdapter chatMessageListenerAdapter
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(cf);
        // chat.room.* 형태의 모든 채널을 구독
        container.addMessageListener(chatMessageListenerAdapter, new PatternTopic(topicPrefix + "*"));
        return container;
    }

    // Redis 수신 메시지를 처리할 리스너 어댑터 (ChatRedisSubscriber.onMessage 호출)
    @Bean
    public MessageListenerAdapter chatMessageListenerAdapter(ChatRedisSubscriber subscriber) {
        // 리플렉션으로 subscriber.onMessage(Message, byte[]) 호출
        return new MessageListenerAdapter(subscriber, "onMessage");
    }
}
