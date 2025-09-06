package com.example.demo.global.kakao.controller;

import com.example.demo.global.kakao.dto.KakaoTokenResponse;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange(accept = "application/json")
public interface KakaoAuthHttp {

    @PostExchange(url = "/oauth/token", contentType = "application/x-www-form-urlencoded")
    KakaoTokenResponse token(
            @RequestParam("grant_type") String grantType,        // "authorization_code"
            @RequestParam("client_id") String clientId,
            @RequestParam("code") String code,
            @RequestParam("redirect_uri") String redirectUri,    //  필수: 추가됨
            @RequestParam("code_verifier") String codeVerifier   // PKCE
    );
}
