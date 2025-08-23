package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.example")
@EnableJpaAuditing
@EnableScheduling
public class MainServerApplication {
	public static void main(String[] args) {
		SpringApplication.run(MainServerApplication.class, args);
	}
}
