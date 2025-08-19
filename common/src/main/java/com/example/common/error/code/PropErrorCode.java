package com.example.common.error.code;

import org.springframework.http.HttpStatus;

public enum PropErrorCode implements ErrorCode {

    PROP_NOT_FOUND(HttpStatus.NOT_FOUND, "PROP-404", "해당 장식을 찾을 수 없습니다."),
    PROP_R2_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "PROP-500", "R2 서버 오류입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    PropErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    @Override public HttpStatus status() { return status; }
    @Override public String code() { return code; }
    @Override public String message() { return message; }
}
