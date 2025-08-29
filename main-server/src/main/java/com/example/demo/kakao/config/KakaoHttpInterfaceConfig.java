package com.example.demo.kakao.config;

import com.example.demo.kakao.controller.KakaoApiHttp;
import com.example.demo.kakao.controller.KakaoAuthHttp;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class KakaoHttpInterfaceConfig {

    private RestClient buildRestClient(String baseUrl) {
        SimpleClientHttpRequestFactory httpRequestFactory = new SimpleClientHttpRequestFactory();
        httpRequestFactory.setConnectTimeout((int) Duration.ofSeconds(2).toMillis());
        httpRequestFactory.setReadTimeout((int) Duration.ofSeconds(3).toMillis());

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(httpRequestFactory)
                // 기본 컨버터 목록 유지 + Form 컨버터만 추가
                .messageConverters(converters -> {
                    // 기본 컨버터(문자열, Jackson 등)는 이미 들어있음
                    converters.add(new FormHttpMessageConverter());
                })
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
