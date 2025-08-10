package com.example.demo.domain.cd.controller;

import com.example.common.error.exception.NotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CdController {

    @GetMapping("/test-not-found")
    public String testNotFound() {
        // 일부러 예외 발생
        throw new NotFoundException("해당 리소스를 찾을 수 없습니다.");
    }
}
