package com.example.demo.global.aws;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    @GetMapping("/chat/health")
    public String ok() { return "CHAT OK"; }
}