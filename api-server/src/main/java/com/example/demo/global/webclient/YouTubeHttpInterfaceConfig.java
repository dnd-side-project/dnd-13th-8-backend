package com.example.demo.global.webclient;

import com.example.demo.domain.song.controller.YouTubeApiHttp;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
@RequiredArgsConstructor
public class YouTubeHttpInterfaceConfig {

    private final YouTubeWebClientFilter webClientFilter;

    @Value("${youtube.api.base-url}")
    private String baseUrl;

    @Bean
    public YouTubeApiHttp youTubeApiHttp() {
        WebClient webClient = webClientFilter.create(baseUrl);

        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor((WebClientAdapter.create(webClient)))
                .build();


        return factory.createClient(YouTubeApiHttp.class);
    }
}

