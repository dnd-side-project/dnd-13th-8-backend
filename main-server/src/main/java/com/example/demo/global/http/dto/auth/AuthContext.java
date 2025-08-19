package com.example.demo.global.http.dto.auth;

public record AuthContext(
        String userName,
        String refreshJwt,
        String sessionName
) {}