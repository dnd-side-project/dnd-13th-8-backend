package com.example.demo.kakao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoProfileResponse(
        Long id,
        KakaoAccount kakao_account
) {
    public record KakaoAccount(
            Profile profile
    ) {}

    public record Profile(
            String nickname,

            @JsonProperty("profile_image_url")
            String profileImageUrl
    ) {}
}
