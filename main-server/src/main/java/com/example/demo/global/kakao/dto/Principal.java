package com.example.demo.global.kakao.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Builder
public class Principal {
    private final String kakaoId;
    private final String username;
}
