package com.example.demo.kakao;

import com.example.demo.domain.user.dto.KakaoProfileResponse;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange(accept = "application/json")
public interface KakaoApiHttp {

    @GetExchange("/v2/user/me")
    KakaoProfileResponse me(@RequestHeader("Authorization") String bearer);
}