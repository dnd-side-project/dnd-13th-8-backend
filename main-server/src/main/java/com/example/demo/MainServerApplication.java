package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication(scanBasePackages = "com.example")
@EnableJpaAuditing
public class MainServerApplication {
	public static void main(String[] args) {
		SpringApplication.run(MainServerApplication.class, args);
	}
}
