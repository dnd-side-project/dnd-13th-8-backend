package com.example.demo.global.jwt;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "dulak.jwt")
public record JwtProps(
        String issuer,
        String audience,
        String secretBase64,
        int accessTtlMinutes,
        int refreshTtlDays,
        int leewaySeconds
) {}