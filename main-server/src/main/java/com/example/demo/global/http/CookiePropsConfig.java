package com.example.demo.global.http;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(CookieProps.class)
public class CookiePropsConfig {

    @Bean
    public HttpOnlyCookieUtil httpOnlyCookieUtil(CookieProps cookieProps) {
        return new HttpOnlyCookieUtil(cookieProps);
    }
}
