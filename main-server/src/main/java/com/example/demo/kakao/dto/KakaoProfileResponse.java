package com.example.demo.kakao.dto;


public record KakaoProfileResponse(
        Long id,
        KakaoAccount kakao_account
) {
    public record KakaoAccount(
            Profile profile
    ) {}

    public record Profile(
            String nickname
    ) {}
}
