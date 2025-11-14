package com.example.demo.global.cors;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcCorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 모든 엔드포인트에 CORS 적용
                .allowedOrigins(
                        "http://localhost:5173",
                        "https://deulak-dev.vercel.app",
                        "https://www.deulak.com",
                        "http://192.168.0.9:5173",
                        "http://192.168.0.10:5173",
                        "http://192.168.200.147:5173",
                        "http://169.254.149.247:5173"
                )
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Authorization", "Location")
                .allowCredentials(true)
                .maxAge(3600); // preflight 결과 캐싱 (초 단위)
    }
}
