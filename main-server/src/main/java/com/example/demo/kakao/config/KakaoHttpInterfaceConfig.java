package com.example.demo.kakao.config;

import com.example.demo.kakao.controller.KakaoApiHttp;
import com.example.demo.kakao.controller.KakaoAuthHttp;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class KakaoHttpInterfaceConfig {

    private RestClient buildRestClient(String baseUrl) {
        SimpleClientHttpRequestFactory httpRequestFactory = new SimpleClientHttpRequestFactory();
        httpRequestFactory.setConnectTimeout((int) Duration.ofSeconds(2).toMillis());
        httpRequestFactory.setReadTimeout((int) Duration.ofSeconds(3).toMillis());

        List<HttpMessageConverter<?>> converters = new ArrayList<>();
        converters.add(new FormHttpMessageConverter());

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(httpRequestFactory)
                .messageConverters(converters)
                .build();
    }

    @Bean
    public KakaoAuthHttp kakaoAuthHttp() {
        var rc = buildRestClient("https://kauth.kakao.com");
        var factory = HttpServiceProxyFactory.builderFor(RestClientAdapter.create(rc)).build();
        return factory.createClient(KakaoAuthHttp.class);
    }

    @Bean
    public KakaoApiHttp kakaoApiHttp() {
        var rc = buildRestClient("https://kapi.kakao.com");
        var factory = HttpServiceProxyFactory.builderFor(RestClientAdapter.create(rc)).build();
        return factory.createClient(KakaoApiHttp.class);
    }
}

