package com.example.common.error.code;

import org.springframework.http.HttpStatus;

public interface ErrorCode {
    HttpStatus status();
    String code();     // "USER-404" 등
    String message();  //메시지

}
