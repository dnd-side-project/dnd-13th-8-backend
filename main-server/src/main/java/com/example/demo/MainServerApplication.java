package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(basePackages = {
		"com.example.demo",       // 전체 애플리케이션 루트
		"com.example.common"
		// api-server 패키지 중 제외할 곳은 명시적으로 빼줄 거임
}, excludeFilters = {
		@ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.example\\.demo\\.domain\\.songs\\..*")
})
@EnableJpaAuditing
@EnableScheduling
public class MainServerApplication {
	public static void main(String[] args) {
		SpringApplication.run(MainServerApplication.class, args);
	}
}
