package com.example.common.error.code;

import org.springframework.http.HttpStatus;

public enum JwtErrorCode implements ErrorCode {
    JWT_EXPIRED(HttpStatus.UNAUTHORIZED, "JWT-401-EXPIRED", "토큰이 만료되었습니다."),
    JWT_INVALID(HttpStatus.UNAUTHORIZED, "JWT-401-INVALID", "유효하지 않은 토큰입니다."),
    JWT_MALFORMED(HttpStatus.BAD_REQUEST, "JWT-400-MALFORMED", "토큰 형식이 잘못되었습니다."),
    JWT_SIGNATURE_INVALID(HttpStatus.UNAUTHORIZED, "JWT-401-SIGNATURE", "토큰 서명 검증에 실패했습니다."),
    JWT_UNSUPPORTED(HttpStatus.BAD_REQUEST, "JWT-400-UNSUPPORTED", "지원하지 않는 토큰입니다."),
    JWT_BLACKLISTED(HttpStatus.UNAUTHORIZED, "JWT-401-BLACKLISTED", "사용이 차단된 토큰입니다."),
    JWT_TYP_MISMATCH(HttpStatus.UNAUTHORIZED, "JWT-401-TYP", "토큰 유형이 올바르지 않습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    JwtErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    @Override public HttpStatus status() { return status; }
    @Override public String code() { return code; }
    @Override public String message() { return message; }
}
