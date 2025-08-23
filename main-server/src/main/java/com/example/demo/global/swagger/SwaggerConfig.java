package com.example.demo.global.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI mainServerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Main Server API")
                        .description("메인 서버 문서")
                        .version("v1"));

    }
}
