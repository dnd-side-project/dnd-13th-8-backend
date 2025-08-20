package com.example.demo.global.webclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;

@Slf4j
@Component
public class YouTubeWebClientFilter {

    /**
     * timeout + retry + logging filter가 포함된 WebClient 생성
     */
    public WebClient create(String baseUrl) {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(5)) // 전체 응답 시간 제한
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000) // 연결 시도 제한
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(5))
                        .addHandlerLast(new WriteTimeoutHandler(5))
                );

        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter(logRequest())   // 요청 로그 필터
                .filter(retryFilter())  // 재시도 필터
                .build();
    }

    /**
     * 요청 로그 필터
     */
    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(request -> {
            log.debug("[WebClient] 요청: {} {}", request.method(), request.url());
            return Mono.just(request);
        });
    }

    /**
     * 재시도 필터 (5xx 또는 429 응답 시 최대 3회 지수 백오프 재시도)
     */
    private ExchangeFilterFunction retryFilter() {
        return ExchangeFilterFunction.ofResponseProcessor(response -> {
            if (response.statusCode().is5xxServerError() || response.statusCode().value() == 429) {
                return response.createException().flatMap(Mono::error);
            }
            return Mono.just(response);
        }).andThen((request, next) ->
                next.exchange(request)
                        .retryWhen(Retry.backoff(3, Duration.ofMillis(500))
                                .maxBackoff(Duration.ofSeconds(5))
                                .filter(this::isRetryable)
                                .onRetryExhaustedThrow((retrySpec, signal) -> signal.failure()))
        );
    }

    /**
     * 재시도 대상 오류 여부 판단
     */
    private boolean isRetryable(Throwable throwable) {
        return throwable instanceof TimeoutException ||
                throwable instanceof IOException ||
                throwable.getMessage().contains("5xx") ||
                throwable.getMessage().contains("429");
    }
}
