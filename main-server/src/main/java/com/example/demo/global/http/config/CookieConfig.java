package com.example.demo.global.http.config;

import com.example.demo.global.http.dto.CookieProps;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(CookieProps.class)
public class CookieConfig {
}