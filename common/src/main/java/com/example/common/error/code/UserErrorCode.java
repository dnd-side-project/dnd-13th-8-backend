package com.example.common.error.code;

import org.springframework.http.HttpStatus;

public enum UserErrorCode implements ErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER-404", "사용자를 찾을 수 없습니다."),
    USER_CONFLICT(HttpStatus.CONFLICT, "USER-409", "이미 존재하는 사용자입니다."),
    USER_INACTIVE(HttpStatus.FORBIDDEN, "USER-403-INACTIVE", "비활성화된 사용자입니다."),
    USER_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "USER-401", "인증이 필요합니다."),
    DUPLICATED_USERNAME(HttpStatus.CONFLICT, "USER-409-DUPLICATE", "이미 사용 중인 닉네임입니다.");  // ✅ 추가된 항목

    private final HttpStatus status;
    private final String code;
    private final String message;

    UserErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    @Override public HttpStatus status() { return status; }
    @Override public String code() { return code; }
    @Override public String message() { return message; }
}
