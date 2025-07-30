package com.example.demo.testController;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api-server")
    public String test(){
        return "api-server 모듈 테스스트!!!!!!!!";
    }
}
