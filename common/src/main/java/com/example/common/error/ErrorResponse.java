package com.example.common.error;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ErrorResponse {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private final LocalDateTime timeStamp;

    private final String code;

    private final ErrorDetail error;
    @Getter
    public static class ErrorDetail {
        private final String type;
        private final String message;

        public ErrorDetail(String type, String message) {
            this.type = type;
            this.message = message;
        }
    }

    public ErrorResponse(ExceptionBase ex) {
        this.timeStamp = LocalDateTime.now();
        this.code = ex.getErrorCode().name(); // enum 값
        this.error = new ErrorDetail(
                ex.getClass().getSimpleName(), // 예외 클래스명
                ex.getMessage()
        );
    }
}