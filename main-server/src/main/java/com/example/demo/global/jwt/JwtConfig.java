package com.example.demo.global.jwt;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(JwtProps.class)
public class JwtConfig {
    @Bean
    public JwtProvider jwtProvider(JwtProps props) {
        return new JwtProvider(props);
    }
}
