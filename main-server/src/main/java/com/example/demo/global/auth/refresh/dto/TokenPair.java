package com.example.demo.global.auth.refresh.dto;


public record TokenPair(String accessToken, String refreshToken, long refreshTtlSeconds) {}

