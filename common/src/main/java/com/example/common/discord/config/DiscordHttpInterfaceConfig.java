package com.example.common.discord.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.time.Duration;

@Configuration
public class DiscordHttpInterfaceConfig {

    @Bean
    public DiscordWebhookHttp discordWebhookHttp(
            @Value("${discord.webhook-url}") String webhookUrl
    ) {
        SimpleClientHttpRequestFactory httpRequestFactory = new SimpleClientHttpRequestFactory();
        httpRequestFactory.setConnectTimeout((int) Duration.ofSeconds(2).toMillis());
        httpRequestFactory.setReadTimeout((int) Duration.ofSeconds(3).toMillis());

        RestClient restClient = RestClient.builder()
                .baseUrl(webhookUrl)
                .requestFactory(httpRequestFactory)
                .build();

        HttpServiceProxyFactory factory =
                HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient)).build();

        return factory.createClient(DiscordWebhookHttp.class);
    }
}
