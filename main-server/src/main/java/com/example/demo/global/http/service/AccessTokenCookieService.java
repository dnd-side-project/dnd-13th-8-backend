package com.example.demo.global.http.service;

import com.example.demo.global.http.dto.CookieProps;
import com.example.demo.global.http.util.CookieBuilder;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessTokenCookieService {

    private final CookieBuilder builder;
    private final CookieProps props;

    public ResponseCookie create(String jwt) {
        return builder.build(props.access().name(), jwt,
                Duration.ofMinutes(props.access().ttlMinutes()), props.access().path());
    }

    public ResponseCookie clear() {
        return builder.expire(props.access().name(), props.access().path());
    }
}
