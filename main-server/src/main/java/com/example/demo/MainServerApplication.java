package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(basePackages = {
		"com.example.demo",
		"com.example.common"
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
