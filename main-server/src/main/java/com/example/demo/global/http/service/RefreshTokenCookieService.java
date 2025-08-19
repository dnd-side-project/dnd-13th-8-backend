package com.example.demo.global.http.service;

import com.example.demo.global.http.dto.CookieProps;

import com.example.demo.global.http.util.CookieBuilder;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RefreshTokenCookieService {

    private final CookieBuilder builder;
    private final CookieProps props;

    public ResponseCookie create(String jwt) {
        return builder.build(
                props.refresh().name(),
                jwt,
                Duration.ofDays(props.refresh().ttlDays()),
                props.refresh().path()
        );
    }

    public ResponseCookie clear() {
        return builder.expire(
                props.refresh().name(),
                props.refresh().path()
        );
    }
}
